package com.example.hotel_admin_service.service;

import com.example.hotel_admin_service.exception.HotelNotFoundException;
import com.example.hotel_admin_service.feign.RoomInterface;
import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.*;
import com.example.hotel_admin_service.rabbitMessagePublisher.SyncHotelPublish;
import com.example.hotel_admin_service.repository.HotelRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class HotelServiceImp implements HotelService {

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    RoomInterface roomInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SyncHotelPublish syncHotelPublish;

    @Autowired
    RedisTemplate<String, Hotel> redisTemplate;

    private BoundHashOperations<String, String, Hotel> getHotelOperation() {
        return redisTemplate.boundHashOps("allHotels");
    }

    private List<String> extractPictures(Map<String, String> pictureMap) {
        List<String> pictures = new ArrayList<>();
        if (pictureMap != null) {
            if (pictureMap.get("firstPic") != null)
                pictures.add(pictureMap.get("firstPic"));
            if (pictureMap.get("secondPic") != null)
                pictures.add(pictureMap.get("secondPic"));
            if (pictureMap.get("thirdPic") != null)
                pictures.add(pictureMap.get("thirdPic"));
        }
        return pictures;
    }

    private Address extractAddress(Address oldAddress) {
        if (oldAddress != null) {
            Address newAddress = new Address();
            newAddress.setCity(oldAddress.getCity());
            newAddress.setDistrict(oldAddress.getDistrict());
            newAddress.setStreet(oldAddress.getStreet());
            newAddress.setNumber(oldAddress.getNumber());
            return newAddress;
        }
        return new Address();
    }

    @CacheEvict(value = "city", allEntries = true)
    @Transactional
    @Override
    public HotelDto createHotel(Long bossId, CreateHotelRequest request) {

        Hotel newHotel = new Hotel();
        newHotel.setChName(request.getChName());
        newHotel.setEnName(request.getEnName());
        newHotel.setIntroduction(request.getIntroduction());
        List<String> facilities = request.getFacilities();
        newHotel.setFacilities(facilities);
        newHotel.setBossId(bossId);
        newHotel.setScore(0);
        newHotel.setBuildDate(LocalDateTime.now(Clock.systemUTC()));
        Address address = extractAddress(request.getAddress());
        newHotel.setAddress(address);
        List<String> pictures = extractPictures(request.getPictures());
        newHotel.setPictures(pictures);
        Hotel buildHotel = hotelRepository.save(newHotel);

        getHotelOperation().put(buildHotel.getHotelId() + "", buildHotel);
        log.info("(createHotel)旅店建立完成，存入redis");

        try {
            syncHotelPublish.sendMsg(new CustomRabbitMessage("createHotelRoute", "hotelExchange", buildHotel));
        } catch (Exception e) {
            log.error("(createHotel) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return convertHotelsToHotelDto(newHotel);
    }

    @CacheEvict(value = "city", allEntries = true)
    @Transactional
    @Override
    public boolean deleteHotelByHotelId(Long hotelId) throws HotelNotFoundException {
        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        if (hotel.isPresent()) {
            hotelRepository.deleteById(hotelId);
            log.info("(deleteHotelByHotelId)hotelId" + hotelId + "已成功刪除");
            getHotelOperation().delete(hotelId + "");
            try {
                syncHotelPublish.sendMsg(new CustomRabbitMessage("deleteHotelRoute", "hotelExchange", hotelId));
            } catch (Exception e) {
                log.error("(deleteRoomsByRoomIds) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
                return false;
            }
            return true;
        } else
            throw new HotelNotFoundException(hotelId);
    }

    public HotelCardDto convertHotelToHotelCardDto(Hotel hotel) {
        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setBossId(hotel.getBossId());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        List<String> roomNames = roomInterface.findRoomNamesByHotelId(hotel.getHotelId()).getBody().getData();
        if (roomNames != null)
            hc.setRoomName(roomNames);
        if (!hotel.getPictures().isEmpty())
            hc.setPicture(hotel.getPictures().get(0));
        return hc;
    }

    @Override
    public List<HotelCardDto> findHotelsByBoss(Long bossId) throws HotelNotFoundException {
        List<Hotel> myHotels = hotelRepository.findByBossId(bossId);
        if (myHotels.isEmpty())
            return new ArrayList<>();
        List<HotelCardDto> newHotels = new ArrayList<>();
        for (Hotel hotel : myHotels) {
            newHotels.add(convertHotelToHotelCardDto(hotel));
        }
        return newHotels;
    }

    @CacheEvict(value = "city", allEntries = true)
    @Transactional
    @Override
    public HotelDto updateHotelData(Long hotelId, CreateHotelRequest request) throws HotelNotFoundException {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(
                () -> new HotelNotFoundException(hotelId)
        );
        hotel.setChName(request.getChName());
        hotel.setEnName(request.getEnName());
        hotel.setIntroduction(request.getIntroduction());
        hotel.setFacilities(request.getFacilities());
        Address address = extractAddress(request.getAddress());
        hotel.setAddress(address);
        List<String> pictures = extractPictures(request.getPictures());
        hotel.setPictures(pictures);

        getHotelOperation().put(hotelId + "", hotel);
        try {
            syncHotelPublish.sendMsg(new CustomRabbitMessage("updateHotelRoute", "hotelExchange", hotel));
        } catch (Exception e) {
            log.error("(deleteRoomsByRoomIds) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return convertHotelsToHotelDto(hotelRepository.save(hotel));
    }

    private HotelDto convertHotelsToHotelDto(Hotel hotel) {
        return objectMapper.convertValue(hotel, HotelDto.class);
    }

    @Override
    public HotelDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException {
        Hotel hFromRedis = getHotelOperation().get(hotelId + "");
        if (hFromRedis != null) {
            log.info("(findHotelDtoByHotelId)自redis取得");
            return convertHotelsToHotelDto(hFromRedis);
        }

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));

        getHotelOperation().put(hotelId + "", hotel);
        return convertHotelsToHotelDto(hotel);
    }

//    @Override
//    public HotelDto updateHotelScore(Long hotelId, Double score) throws HotelNotFoundException {
//        HotelDto hotelDto = findHotelDtoByHotelId(hotelId);
//        hotelDto.setScore(score);
//        Hotel hotel = objectMapper.convertValue(hotelDto, Hotel.class);
//        hotelRepository.save(hotel);
//        getHotelOperation().put(hotelId + "", hotel);
//        return hotelDto;
//    }

    @Override
    public Boolean validateBoss(Long userId, Long hotelId) throws HotelNotFoundException {
        HotelDto hotel = findHotelDtoByHotelId(hotelId);

        return Objects.equals(hotel.getBossId(), userId);
    }

    @Override
    public List<Long> findHotelIdsByBossId(Long bossId) {
        return hotelRepository.findHotelIdsByBossId(bossId);
    }

    @Override
    public String findHotelNameByHotelId(Long hotelId) throws HotelNotFoundException {
        HotelDto hotel = findHotelDtoByHotelId(hotelId);
        return hotel.getChName();
    }

    @Override
    public List<GetHotelRoomResquest> findHotelsWithRoomsByBoss(String jwt,Long userId) throws HotelNotFoundException {
        List<Long> hotelIds = findHotelIdsByBossId(userId);
        log.info("(findHotelsWithRoomsByBoss)找到老闆的{}間旅店",hotelIds.size());
        return hotelIds.stream().map(id-> {
            try {
                return findHotelNameAndRoomById(jwt,id);
            } catch (HotelNotFoundException e) {
                log.error("(findHotelsWithRoomsByBoss)"+e.getMsg());
                throw new RuntimeException(e);
            }
        }).toList();
    }

    private GetHotelRoomResquest findHotelNameAndRoomById(String jwt,Long hotelId) throws HotelNotFoundException {
        String hotel = findHotelNameByHotelId(hotelId);
        List<Long> roomIds = roomInterface.findRoomIdsByHotelId(jwt,hotelId).getBody().getData();
        return new GetHotelRoomResquest(hotel,roomIds);
    }

    @RabbitListener(queues = "updateAdminHotelScoreQueue")
    public void updateHotelScore(String hotelScoreData) throws HotelNotFoundException {
        log.info("(updateHotelScore)我收到更新旅店分數的資料是{}...",hotelScoreData);
        //接到的資料為hotel:{hotelId},score:{score}
        String[] dataArray = hotelScoreData.split(",");
        long hotelId = Long.parseLong(dataArray[0].substring(6));
        double score = Double.parseDouble(dataArray[1].substring(6));
        log.info("(updateHotelScore)拆分成hotel是"+hotelId+"以及score是"+score);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new HotelNotFoundException(hotelId));
        hotel.setScore(score);
        Hotel newHotel = hotelRepository.save(hotel);
        getHotelOperation().put(hotelId+"",newHotel);
        log.info("(updateHotelScore)旅店{}分數{}更新成功", hotelId, score);
    }
}
