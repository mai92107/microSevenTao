package com.example.hotel_admin_service.service;

import com.example.hotel_admin_service.feign.CommentInterface;
import com.example.hotel_admin_service.feign.RoomInterface;
import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.Comment;
import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.model.dto.HotelCardDto;
import com.example.hotel_admin_service.model.dto.HotelDto;
import com.example.hotel_admin_service.model.dto.RoomDto;
import com.example.hotel_admin_service.repository.AddressRepository;
import com.example.hotel_admin_service.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class HotelServiceImp implements HotelService {

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    RoomInterface roomInterface;

    @Autowired
    CommentInterface commentInterface;

    @Override
    public Hotel createHotel(Long bossId, CreateHotelRequest request) {

        Hotel newHotel = new Hotel();
        newHotel.setChName(request.getChName());
        newHotel.setEnName(request.getEnName());
        newHotel.setIntroduction(request.getIntroduction());
        List<String> facilities = request.getFacilities();
        newHotel.setFacilities(facilities);
        newHotel.setBossId(bossId);
        newHotel.setScore(0);
        newHotel.setBuildDate(LocalDateTime.now(Clock.systemUTC()));
        Address address = request.getAddress();
        if (address != null) {
            Address newAddress = new Address();
            newAddress.setCity(address.getCity());
            newAddress.setDistrict(address.getDistrict());
            newAddress.setStreet(address.getStreet());
            newAddress.setNumber(address.getNumber());
            newHotel.setAddress(newAddress);
        }

        Map<String, String> pictureMap = request.getPictures();
        List<String> pictures = new CopyOnWriteArrayList<>();

        if (pictureMap != null) {
            if (pictureMap.get("firstPic") != null)
                pictures.add(pictureMap.get("firstPic"));
            if (pictureMap.get("secondPic") != null)
                pictures.add(pictureMap.get("secondPic"));
            if (pictureMap.get("thirdPic") != null)
                pictures.add(pictureMap.get("thirdPic"));
        }
        newHotel.setPictures(pictures);
        hotelRepository.save(newHotel);
        return newHotel;
    }

    @Override
    public boolean deleteHotelByHotelId(Long hotelId) {
        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        if (hotel.isPresent()) {
            hotelRepository.deleteById(hotelId);
            System.out.println("hotelId" + hotelId + "已成功刪除");
            return true;
        } else
            return false;
    }

    public HotelCardDto convertHotelToHotelCardDto(Hotel hotel) {
        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        System.out.println("轉換中..."+hotel.getHotelId());
        List<String> roomNames = roomInterface.findRoomNamesByHotelId(hotel.getHotelId()).getBody();
        System.out.println("接收到"+roomNames);
        if (roomNames != null)
            hc.setRoomName(roomNames);
        if (!hotel.getPictures().isEmpty())
            hc.setPicture(hotel.getPictures().get(0));
        return hc;
    }

    @Override
    public List<HotelCardDto> findHotelsByBoss(Long bossId) {
        List<Hotel> myHotels = hotelRepository.findByBossId(bossId);
        List<HotelCardDto> newHotels = new ArrayList<>();
        for (Hotel hotel : myHotels) {
            System.out.println("旅店-" + hotel.getChName());
            newHotels.add(convertHotelToHotelCardDto(hotel));
            System.out.println("轉換完畢");
        }
        return newHotels;
    }

    @Override
    public Hotel findHotelByHotelId(Long hotelId) {
        return hotelRepository.findById(hotelId).orElse(null);
    }

    @Override
    public Hotel updateHotelData(Long hotelId, CreateHotelRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(
                () -> new RuntimeException("Hotel not found with id: " + hotelId)
        );

        hotel.setChName(request.getChName());
        hotel.setEnName(request.getEnName());
        hotel.setIntroduction(request.getIntroduction());
        hotel.setFacilities(request.getFacilities());
        Address address = new Address();
        address.setCity(request.getAddress().getCity());
        address.setDistrict(request.getAddress().getDistrict());
        address.setStreet(request.getAddress().getStreet());
        address.setNumber(request.getAddress().getNumber());
        hotel.setAddress(address);

        List<String> pictures = new CopyOnWriteArrayList<>();
        if (request.getPictures().get("firstPic") != null)
            pictures.add(request.getPictures().get("firstPic"));
        if (request.getPictures().get("secondPic") != null)
            pictures.add(request.getPictures().get("secondPic"));
        if (request.getPictures().get("thirdPic") != null)
            pictures.add(request.getPictures().get("thirdPic"));

        hotel.setPictures(pictures);
        return hotelRepository.save(hotel);
    }

    private HotelDto convertHotelsToHotelDto(Hotel hotel) {

        HotelDto dto = new HotelDto();
        dto.setHotelId(hotel.getHotelId());
        dto.setBossId(hotel.getBossId());
        dto.setPictures(hotel.getPictures());
        dto.setChName(hotel.getChName());
        dto.setEnName(hotel.getEnName());
        dto.setIntroduction(hotel.getIntroduction());
        dto.setFacilities(hotel.getFacilities());
        dto.setAddress(hotel.getAddress());
        List<RoomDto> hotelRooms = roomInterface.findRoomsByHotelId(hotel.getHotelId()).getBody();
        if (hotelRooms != null)
            dto.setRooms(hotelRooms);
        List<Comment> hotelComments = commentInterface.getHotelComments(hotel.getHotelId()).getBody();
        if (hotelComments != null)
            dto.setComments(hotelComments);
        dto.setScore(hotel.getScore());
        return dto;
    }

    @Override
    public HotelDto findHotelDtoByHotelId(Long hotelId) {
        return convertHotelsToHotelDto(hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("查無此旅店")));
    }

    @Override
    public void updateHotelScore(Long hotelId, Double score) {
        Hotel hotel = findHotelByHotelId(hotelId);
        hotel.setScore(score);
        hotelRepository.save(hotel);
    }

    @Override
    public Boolean validateBoss(Long userId, Long hotelId) {
        Hotel hotel = findHotelByHotelId(hotelId);

        return Objects.equals(hotel.getBossId(), userId);
    }

    @Override
    public List<Long> findHotelIdsByBossId(Long userId) {
        return hotelRepository.findHotelIdsByBossId(userId);
    }
}
