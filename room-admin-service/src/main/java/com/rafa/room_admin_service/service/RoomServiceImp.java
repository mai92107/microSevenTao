package com.rafa.room_admin_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.room_admin_service.exception.RoomNotFoundException;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.CustomRabbitMessage;
import com.rafa.room_admin_service.model.dto.RoomDto;
import com.rafa.room_admin_service.rabbitMessagePublisher.SyncRoomPublish;
import com.rafa.room_admin_service.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomServiceImp implements RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisTemplate<String, Room> redisTemplate;

    @Autowired
    SyncRoomPublish syncRoomPublish;


    private String generateRoomKey(Long hotelId, Long roomId) {
        String hotel = hotelId == null ? "*" : hotelId.toString();
        String room = roomId == null ? "*" : roomId.toString();
        String hotelPart = "hotel:" + hotel;
        String roomPart = "room:" + room;

        return hotelPart + "," + roomPart;
    }

    @Transactional
    @Override
    public List<Room> createRooms(Long hotelId, List<CreateRoomRequest> requests) {
        List<Room> rooms = new ArrayList<>();
        for (CreateRoomRequest request : requests) {
            Room room = new Room();
            room.setRoomName(request.getRoomName());
            room.setSpecialties(request.getSpecialties());
            room.setCapacity(request.getCapacity());
            room.setRoomSize(request.getRoomSize());
            room.setHotelId(hotelId);
            room.setPrices(new ArrayList<>(request.getPrices()));
            room.setRoomPic(new ArrayList<>(request.getRoomPic()));
            rooms.add(room);
        }
        List<Room> newRooms = roomRepository.saveAllAndFlush(rooms);

        Map<String, Room> roomMap =
                newRooms.stream().collect(Collectors.toMap(
                        r -> generateRoomKey(r.getHotelId(), r.getRoomId()),
                        r -> r));
        redisTemplate.opsForValue().multiSet(roomMap);
        log.info("(createRooms)儲存房間成功數量" + roomMap.size());

        try {
            syncRoomPublish.sendMsg(new CustomRabbitMessage("createRoomRoute", "roomExchange", newRooms));
        } catch (Exception e) {
            log.error("(createRooms) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return newRooms;
    }


    @Transactional
    @Override
    public boolean deleteRoomsByRoomIds(List<Long> roomIds) {
        try {
            log.info("(deleteRoomsByRoomIds)接獲刪除資料數量" + roomIds.size());
            Long hotelId = findHotelIdByRoomId(roomIds.get(0));
            roomRepository.deleteAllByIdInBatch(roomIds);
            Set<String> keys = redisTemplate.keys(generateRoomKey(hotelId, null));
            log.info("(deleteRoomsByRoomIds)redis中hotel旅店有幾間房間" + keys.size());
            for (Long roomId : roomIds) {
                boolean redisDeleted = Boolean.TRUE.equals(redisTemplate.delete(generateRoomKey(hotelId, roomId)));
                if (!redisDeleted) {
                    log.error("(deleteRoomsByRoomIds) Redis 中刪除 roomId {} 失敗", roomId);
                    return false; // 若 Redis 刪除失敗則返回 false
                }
            }
            try {
                syncRoomPublish.sendMsg(new CustomRabbitMessage("deleteRoomRoute", "roomExchange", roomIds));
            } catch (Exception e) {
                log.error("(deleteRoomsByRoomIds) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("(deleteRoomsByRoomIds)" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Long findHotelIdByRoomId(Long roomId) throws RoomNotFoundException {
        Room room = roomRepository.findById(roomId)
                .orElse(null);
        if (room == null)
            throw new RoomNotFoundException(roomId);
        return room.getHotelId();
    }

    @Override
    public List<Long> getRoomIdsByHotelId(Long hotelId) throws RoomNotFoundException {
        Set<String> keys = redisTemplate.keys(generateRoomKey(hotelId, null));
        if (keys != null && !keys.isEmpty()) {
            log.info("(getRoomIdsByHotelId)自redis取room資料");
            return redisTemplate.opsForValue().multiGet(keys)
                    .stream().filter(Objects::nonNull)
                    .map(Room::getRoomId).collect(Collectors
                            .toList());
        }
        log.info("(getRoomIdsByHotelId)自資料庫取room資料");
        List<Long> roomIds = roomRepository.findRoomIdsByHotelId(hotelId);

        return roomIds;
    }

    @Override
    public List<RoomDto> findRoomByHotelId(Long hotelId) throws RoomNotFoundException {
        Set<String> keys = redisTemplate.keys(generateRoomKey(hotelId, null));
        if (keys != null && !keys.isEmpty()) {
            log.info("(findRoomByHotelId)自redis取room資料");
            List<Room> rooms = redisTemplate.opsForValue().multiGet(keys);
            if (rooms != null && !rooms.isEmpty())
                return rooms.stream().map(r -> objectMapper.convertValue(r, RoomDto.class)).toList();
        }
        log.info("(findRoomByHotelId)自資料庫取room資料");
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        if (rooms.isEmpty())
            return null;
        redisTemplate.opsForValue().multiSet(
                rooms.stream()
                        .collect(Collectors.toMap(
                                r -> generateRoomKey(r.getHotelId(), r.getRoomId()),
                                r -> r
                        ))
        );

        return rooms.stream().map(r ->
                objectMapper.convertValue(r, RoomDto.class)
        ).toList();
    }

    @Override
    public List<String> findRoomNamesByHotelId(Long hotelId) throws RoomNotFoundException {
        Set<String> keys = redisTemplate.keys(generateRoomKey(hotelId, null));
        if (keys != null && !keys.isEmpty()) {
            log.info("(findRoomNamesByHotelId)自redis取room資料");
            return redisTemplate.opsForValue().multiGet(keys)
                    .stream().filter(Objects::nonNull)
                    .map(Room::getRoomName).collect(Collectors
                            .toList());
        }
        log.info("(findRoomNamesByHotelId)自資料庫取room資料");
        List<String> roomNames = roomRepository.findRoomNameByHotelId(hotelId);


        return roomNames;
    }

}
