package com.rafa.room_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.room_service.exception.LivingDateErrorException;
import com.rafa.room_service.exception.RoomException;
import com.rafa.room_service.exception.RoomNotFoundException;
import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.model.roomDto.RoomDto;
import com.rafa.room_service.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoomServiceImp implements RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisTemplate<String, Room> redisTemplate;

    @Autowired
    Environment environment;

    private List<RoomDto> getRoomByRoomIdOrByHotelIdFromRedis(Long hotelId, Long roomId) {
        if (hotelId != null && roomId == null) {
            Set<String> roomsKeySet = redisTemplate.keys("hotel:" + hotelId + ",room:*");
            List<RoomDto> hotelRooms = new ArrayList<>();
            if (roomsKeySet == null || roomsKeySet.isEmpty()) {
                log.info("(getRoomByRoomIdOrByHotelIdFromRedis)未在redis中找到，返回空陣列");
                return hotelRooms;
            }
            log.info("(getRoomByRoomIdOrByHotelIdFromRedis)用hotelId找房間，自redis取得hotel的room資料");
            for (String roomKey : roomsKeySet) {
                hotelRooms.add(objectMapper.convertValue(redisTemplate.opsForValue().get(roomKey), RoomDto.class));
            }
            ;
            log.info("(getRoomByRoomIdOrByHotelIdFromRedis)用hotelId找房間，自redis獲得房間幾間" + hotelRooms.size());
            return hotelRooms;
        }

        if (hotelId == null && roomId != null) {
            Set<String> roomKeySet = redisTemplate.keys("hotel:*,room:" + roomId);
            List<RoomDto> room = new ArrayList<>();
            if (roomKeySet == null || roomKeySet.isEmpty()) {
                log.info("(getRoomByRoomIdOrByHotelIdFromRedis)用roomId找房間，未在redis中找到，返回空陣列");
                return room;
            }
            log.info("(getRoomByRoomIdOrByHotelIdFromRedis)用roomId找房間，自redis取得hotel的room資料");
            for (String roomKey : roomKeySet) {
                room.add(objectMapper.convertValue(redisTemplate.opsForValue().get(roomKey), RoomDto.class));
            }
            ;
            log.info("(getRoomByRoomIdOrByHotelIdFromRedis)用roomId找房間，自redis獲得房間幾間" + room.size());
            return room;
        }
        return null;
    }


    @Override
    public List<RoomDto> findRoomByHotelId(Long hotelId) {

        List<RoomDto> hotelRooms = getRoomByRoomIdOrByHotelIdFromRedis(hotelId, null);
        if (!hotelRooms.isEmpty())
            return hotelRooms;

        log.info("(findRoomByHotelId)redis無此資料，存入redis此旅店所有房間" + hotelId);
        List<Room> hotelRoom = roomRepository.findByHotelId(hotelId);
        List<RoomDto> hotelRoomDto = new ArrayList<>();
        for (Room r : hotelRoom) {
            redisTemplate.opsForValue().set("hotel:" + hotelId + ",room:" + r.getRoomId(), r);
            hotelRoomDto.add(objectMapper.convertValue(r, RoomDto.class));
        }
        return hotelRoomDto;
    }

    public Optional<RoomDto> findRoomById(Long roomId) {
        log.info("(findRoomById)搜尋此id的房間" + roomId);
        List<RoomDto> roomRedis = getRoomByRoomIdOrByHotelIdFromRedis(null, roomId);
        if (!roomRedis.isEmpty())
            return Optional.of(roomRedis.get(0));
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            log.error("(findRoomById)查無此房間" + roomId);
            return Optional.empty();
        }
        log.info("(findRoomById)redis無此資料，存入redis房間" + roomId);
        Room findRoom = room.get();
        redisTemplate.opsForValue().set("hotel:" + findRoom.getHotelId() + ",room:" + roomId, findRoom);
        return Optional.of(objectMapper.convertValue(findRoom, RoomDto.class));
    }


    private Integer countPriceByLivingDate(Long roomId, List<LocalDate> dates) throws RoomNotFoundException, LivingDateErrorException {
        Optional<RoomDto> room = findRoomById(roomId);
        if (room.isEmpty())
            throw new RoomNotFoundException(roomId);
        if (dates == null) {
            log.error("(countPriceByLivingDate)ERROR 無查詢房間或日期" + roomId);
            throw new LivingDateErrorException(dates);
        }
        log.info("(countPriceByLivingDate)計算價格的房間名為 " + room.get().getRoomName());

        int totalPrice = 0;
        log.info("(countPriceByLivingDate)計算居住天數為 " + dates.size());

        if (!dates.isEmpty()) {
            List<Integer> priceList = dates.parallelStream()
                    .map(d -> countByDate(room.get().getPrices(), d))
                    .toList();
            log.info("(countPriceByLivingDate)本次住宿每日價格表為" + priceList);
            totalPrice = priceList.stream()
                    .mapToInt(p -> p)
                    .sum();
        }
        log.info("(countPriceByLivingDate)本次住宿總計：" + totalPrice);
        return totalPrice;
    }

    private Integer countByDate(List<Integer> roomPrices, LocalDate date) {
        if (roomPrices == null || roomPrices.isEmpty()) {
            log.info("(countByDate)無房間價格表");
            return 0;
        }
        DayOfWeek chosenDayOfWeek = date.getDayOfWeek();
        log.info("(countByDate)查詢" + chosenDayOfWeek + "房間本日金額是" + roomPrices.get(chosenDayOfWeek.getValue() - 1));
        return roomPrices.get(chosenDayOfWeek.getValue() - 1);
    }

    private List<LocalDate> countLivingDayList(LocalDate start, LocalDate end) throws LivingDateErrorException {
        if (start == null && end == null) {
            log.info("(countLivingDayList)無查詢住宿時間");
            return null;
        }
        if (!start.isBefore(end)) {
            log.info("(countLivingDayList)Error 無法查詢此起迄時間" + start + "到" + end);
            throw new LivingDateErrorException(start, end);
        }
        log.info("(countLivingDayList)本次住宿起迄時間為" + start + "到" + end);
        List<LocalDate> livingDays = new ArrayList<>();
        while (start.isBefore(end)) {
            livingDays.add(start);
            start = start.plusDays(1);
        }
        log.info("(countLivingDayList)本次住宿共" + livingDays.size() + "天");

        return livingDays;
    }


    @Override
    public List<Long> filterInvalidRoomByDetails(List<Long> roomIds, Integer people) {
        List<RoomDto> rooms = roomIds.parallelStream().map(roomId ->
                        findRoomById(roomId).orElse(null))
                .filter(Objects::nonNull).toList();
        log.info("(filterInvalidRoomByDetails)傳入房間" + roomIds.size() + "間，篩選人數" + people + "人");
        return rooms.parallelStream().filter(r -> r.getCapacity() >= people).map(RoomDto::getRoomId).collect(Collectors.toList());
    }

    @Override
    public List<RoomCardDto> convertRoomsToRoomCards(List<Long> roomIds, LocalDate start, LocalDate end) {
        return roomIds.stream().map(id ->
        {
            try {
                return convertRoomToRoomCard(id, start, end);
            } catch (RoomException e) {
                log.error("(convertRoomsToRoomCards)" + e.getMsg());
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }


    private RoomCardDto convertRoomToRoomCard(Long roomId, LocalDate start, LocalDate end) throws RoomException {
        Integer totalPrice = null;
        Optional<RoomDto> room = findRoomById(roomId);
        if (room.isEmpty())
            throw new RoomNotFoundException(roomId);
        if (start != null && end != null)
            totalPrice = countPriceByLivingDate(room.get().getRoomId(), countLivingDayList(start, end));

        return new RoomCardDto(
                room.get().getRoomId(),
                new ArrayList<>(room.get().getRoomPic()),
                room.get().getRoomName(),
                new ArrayList<>(room.get().getSpecialties()),
                totalPrice,
                room.get().getRoomSize(),
                room.get().getCapacity(),
                start,
                end
        );
    }

    @Override
    public List<Long> findRoomIdsByHotelId(Long hotelId) {
        System.out.println("目前使用這個port : " + environment.getProperty("server.port"));
        List<RoomDto> hotelRooms = findRoomByHotelId(hotelId);
        return hotelRooms.parallelStream().map(RoomDto::getRoomId).toList();
    }

    @Override
    public List<String> findRoomNamesByRoomIds(List<Long> roomIds) {
        List<String> roomNames = new ArrayList<>();
        roomIds.forEach(id ->
        {
            try {
                Optional<RoomDto> room = findRoomById(id);
                if (room.isEmpty())
                    throw new RoomNotFoundException(id);
                roomNames.add(findRoomById(id).get().getRoomName());
            } catch (RoomNotFoundException e) {
                log.error("(findRoomNamesByRoomIds)" + e.getMessage());
            }
        });
        return roomNames;
    }

    @Override
    public Integer getHotelMinPricePerDay(List<Long> roomIds, LocalDate start, LocalDate end) {
        return roomIds.parallelStream()
                .map(id -> {
                            Optional<Integer> price = getMinPricePerDay(id, start, end);
                            if (price.isEmpty()) {
                                log.error("(getHotelMinPricePerDay)此房間價格為空" + id);
                                return null;
                            }
                            return price.get();
                        }
                ).filter(Objects::nonNull)
                .min(Integer::compareTo).get();
    }

    @Override
    public Long findHotelIdByRoomId(Long roomId) {
        List<RoomDto> room = getRoomByRoomIdOrByHotelIdFromRedis(null, roomId);
        if (room.isEmpty()) {
            log.info("(findHotelIdByRoomId)從資料庫尋找roomId:{}的hotelId", roomId);
            return roomRepository.findHotelIdByRoomId(roomId);
        }
        return room.get(0).getHotelId();
    }

    @Override
    public List<RoomCardDto> getRoomCardsByDetailsFromRoomIds(Long hotelId, Integer people, LocalDate start, LocalDate end) {
        List<Long> roomList = findRoomIdsByHotelId(hotelId);
        if (people != null)
            roomList = filterInvalidRoomByDetails(roomList, people);
        return roomList.stream().map(id -> {
            try {
                return convertRoomToRoomCard(id, start, end);
            } catch (RoomException e) {
                log.error("(getRoomCardsByDetailsFromRoomIds) 轉換失敗");
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    public Optional<Integer> getMinPricePerDay(Long roomId, LocalDate start, LocalDate end) {
        try {
            Optional<RoomDto> room = findRoomById(roomId);
            if (room.isEmpty())
                throw new RoomNotFoundException(roomId);
            List<LocalDate> livingDays = countLivingDayList(start, end);
            List<Integer> prices = room.get().getPrices();

            if (prices == null || prices.isEmpty()) {
                log.error("(getMinPricePerDay) 無房間價格表");
                return Optional.empty();
            }

            return livingDays.parallelStream()
                    .map(date -> prices.get(date.getDayOfWeek().getValue() - 1))
                    .min(Integer::compareTo);
        } catch (RoomNotFoundException e) {
            log.error("(getMinPricePerDay) 查無房間：" + roomId, e);
            return Optional.empty();
        } catch (LivingDateErrorException e) {
            log.error("(getMinPricePerDay) 日期區間有誤：" + start + " 至 " + end, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("(getMinPricePerDay) 其他錯誤：" + e.getMessage(), e);
            return Optional.empty();
        }
    }

    @RabbitListener(queues = "createRoomQueue")
    public void createRooms(List<Room> rooms) {

        List<Room> newRooms = roomRepository.saveAllAndFlush(rooms);
        log.info("(createRooms)儲存房間成功數量" + newRooms.size());

    }

    @RabbitListener(queues = "deleteRoomQueue")
    public void deleteRooms(List<Long> roomIds) {
        roomRepository.deleteAllById(roomIds);
        log.info("(deleteRooms)刪除房間成功數量" + roomIds.size());
    }


}
