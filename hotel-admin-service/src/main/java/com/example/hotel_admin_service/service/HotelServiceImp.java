package com.example.hotel_admin_service.service;

import com.example.hotel_admin_service.exception.HotelNotFoundException;
import com.example.hotel_admin_service.feign.RoomInterface;
import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.model.dto.HotelCardDto;
import com.example.hotel_admin_service.model.dto.HotelDto;
import com.example.hotel_admin_service.repository.HotelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    RedisTemplate<String,Hotel> redisTemplate;

    private BoundHashOperations<String, String, Hotel> getHotelOperation() {
        return redisTemplate.boundHashOps("hotel");
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

    @CacheEvict(value = "city",allEntries = true)
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

        getHotelOperation().put(buildHotel.getHotelId()+"",buildHotel);
        log.info("(createHotel)旅店建立完成，存入redis");

        return convertHotelsToHotelDto(newHotel);
    }
    @CacheEvict(value = "city",allEntries = true)
    @Transactional
    @Override
    public boolean deleteHotelByHotelId(Long hotelId) throws HotelNotFoundException {
        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        if (hotel.isPresent()) {
            hotelRepository.deleteById(hotelId);
            log.info("(deleteHotelByHotelId)hotelId" + hotelId + "已成功刪除");
            getHotelOperation().delete(hotelId+"");
            return true;
        } else
            throw new HotelNotFoundException(hotelId);
    }

    public HotelCardDto convertHotelToHotelCardDto(Hotel hotel) {
        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        List<String> roomNames = roomInterface.findRoomNamesByHotelId(hotel.getHotelId()).getBody();
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
            throw new HotelNotFoundException(bossId,null);
        List<HotelCardDto> newHotels = new ArrayList<>();
        for (Hotel hotel : myHotels) {
            newHotels.add(convertHotelToHotelCardDto(hotel));
        }
        return newHotels;
    }

    @CacheEvict(value = "city",allEntries = true)
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

        getHotelOperation().put(hotelId+"",hotel);

        return convertHotelsToHotelDto(hotelRepository.save(hotel));
    }

    private HotelDto convertHotelsToHotelDto(Hotel hotel) {
        return objectMapper.convertValue(hotel, HotelDto.class);
    }

    @Override
    public HotelDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException {
        Hotel hFromRedis = getHotelOperation().get(hotelId+"");
        if(hFromRedis!=null) {
            log.info("(findHotelDtoByHotelId)自redis取得");
            return convertHotelsToHotelDto(hFromRedis);
        }

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new HotelNotFoundException(hotelId));

        getHotelOperation().put(hotelId+"",hotel);
        return convertHotelsToHotelDto(hotel);
    }

    @Override
    public HotelDto updateHotelScore(Long hotelId, Double score) throws HotelNotFoundException {
        HotelDto hotelDto = findHotelDtoByHotelId(hotelId);
        hotelDto.setScore(score);
        Hotel hotel = objectMapper.convertValue(hotelDto, Hotel.class);
        hotelRepository.save(hotel);
        getHotelOperation().put(hotelId+"",hotel);
        return hotelDto;
    }

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
}
