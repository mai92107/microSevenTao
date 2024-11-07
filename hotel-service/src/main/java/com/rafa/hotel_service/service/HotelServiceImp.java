package com.rafa.hotel_service.service;

import com.rafa.hotel_service.feign.feign.OrderInterface;
import com.rafa.hotel_service.feign.feign.RoomInterface;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.CheckRoomAvailableByOrder;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

@Service
public class HotelServiceImp implements HotelService {

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    RoomInterface roomInterface;


    public Hotel findHotelByHotelId(Long hotelId) {
        return hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("查無此旅店編號" + hotelId));
    }


    public HotelCardDto convertHotelToHotelCardDto(Hotel hotel, LocalDate start, LocalDate end, Integer people) {
        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        List<Long> validRoomIds = roomInterface.findRoomIdsByHotelId(hotel.getHotelId()).getBody();
        if (start != null && end != null && start.isBefore(end))
            validRoomIds = orderInterface.checkHotelAvailableRooms(new CheckRoomAvailableByOrder(validRoomIds, start, end)).getBody();
        if (people != null)
            validRoomIds = roomInterface.filterValidRoomBySize(validRoomIds, people).getBody();
        Integer minPrice = 0;
        if(start !=null && end!=null)
            minPrice = roomInterface.getMinPricePerDay(validRoomIds,start,end).getBody();
        if (minPrice != null)
            hc.setMinPrice(minPrice);

        List<String> roomNames = roomInterface.findRoomNamesByRoomIds(validRoomIds).getBody();

        if (roomNames.isEmpty())
            return null;
        hc.setRoomName(roomNames);
        if (!hotel.getPictures().isEmpty())
            hc.setPicture(hotel.getPictures().get(0));



        return hc;
    }


    public List<HotelCardDto> convertHotelsToHotelCardDtos(List<Hotel> hotels, LocalDate start, LocalDate end, Integer people) {
        List<HotelCardDto> hotelCards = new ArrayList<>();
        for (Hotel h : hotels) {
            HotelCardDto hcDto = convertHotelToHotelCardDto(h, start, end, people);
            if (hcDto != null)
                hotelCards.add(hcDto);
        }
        return hotelCards;
    }

    @Override
    public List<HotelCardDto> findALLHotelsByDetail(Integer cityCode, String keyword, LocalDate start, LocalDate end, Integer people) {
        //首頁用 無論有無條件
        List<Hotel> hotels = hotelRepository.findHotelsByDetail(cityCode, keyword);
        hotels = hotels.stream().filter(h -> !roomInterface.findRoomIdsByHotelId(h.getHotelId()).getBody().isEmpty()).toList();
        return convertHotelsToHotelCardDtos(hotels, start, end, people);
    }

    @Override
    public HotelDetailDto findHotelDtoByHotelId(Long hotelId) {
        //hotel頁面(未經條件轉換)
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("查無此旅店編號" + hotelId));
        HotelDetailDto dto = new HotelDetailDto();
        dto.setHotelId(hotelId);
        dto.setBossId(hotel.getBossId());
        dto.setPictures(hotel.getPictures());
        dto.setChName(hotel.getChName());
        dto.setEnName(hotel.getEnName());
        dto.setIntroduction(hotel.getIntroduction());
        dto.setFacilities(hotel.getFacilities());
        dto.setAddress(hotel.getAddress());
        dto.setScore(hotel.getScore());
        List<Long> roomIds = roomInterface.findRoomIdsByHotelId(hotelId).getBody();
//        List<RoomDto> rooms = roomInterface.getRoomCardsByTimeFromRoomIds(roomIds, null, null).getBody();

//        dto.setValidRooms(rooms);
        return dto;
    }

//    @Override
//    public HotelDetailDto convertHotel(HotelDetailDto dto) {
//        //旅店頁面經查詢後資料
////        System.out.println("查詢資格包含" + start + end + people);
////        List<Long> rooms = roomInterface.findRoomIdsByHotelId(dto.getHotelId()).getBody();
//        rooms = roomInterface.filterValidRoomBySize(rooms, people).getBody();
//        rooms = orderInterface.checkHotelAvailableRooms(
//                new CheckRoomAvailableByOrder(rooms, start, end)).getBody();
////        List<RoomDto> validRooms = new ArrayList<>();
////        if (rooms != null)
////            validRooms = roomInterface.getRoomCardsByTimeFromRoomIds(rooms, start, end).getBody();
////        dto.setValidRooms(validRooms);
//        return dto;
//    }

//    public HotelDetailDto convertHotelsToHotelDto(Hotel hotel) {
//        //hotel頁面用資料(經查詢資料轉換)
//        System.out.println("進入dto轉換");
//
//        HotelDetailDto dto = new HotelDetailDto();
//        dto.setHotelId(hotel.getHotelId());
//        dto.setBossId(hotel.getBossId());
//        dto.setPictures(hotel.getPictures());
//        dto.setChName(hotel.getChName());
//        dto.setEnName(hotel.getEnName());
//        dto.setIntroduction(hotel.getIntroduction());
//        dto.setFacilities(hotel.getFacilities());
//        dto.setAddress(hotel.getAddress());
//        dto.setComments(commentInterface.getHotelComments(hotel.getHotelId()).getBody());
//        dto.setScore(hotel.getScore());
//        System.out.println("通過dto轉換");
//        return dto;
//    }
//
//    ;

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }


    @Override
    public List<HotelCardDto> getFavoriteHotelsByUserId(Long userId) {
        //個人頁面我的最愛
        List<Hotel> favoriteHotels = hotelRepository.findFavoriteHotelsByUserId(userId);
        return convertHotelsToHotelCardDtos(favoriteHotels, null, null, null);
    }

    @Override
    public Boolean checkIsFavorite(Long userId, Long hotelId) {
        List<Hotel> favoriteHotels = hotelRepository.findFavoriteHotelsByUserId(userId);
        return !favoriteHotels.stream().noneMatch(h -> Objects.equals(h.getHotelId(), hotelId));
    }


    private Integer getHotelOrders(Long hotelId) {
        //計算首頁排序
        List<Long> roomIds = roomInterface.findRoomIdsByHotelId(hotelId).getBody();
        Integer hotelOrders = orderInterface.getHotelOrderCount(roomIds).getBody();
        if (hotelOrders == null)
            return 0;
        return hotelOrders;
    }


    private final ToDoubleFunction<Long> compareByScore = (h -> {
        Hotel hotel = findHotelByHotelId(h);
        return hotel.getScore();
    });
    private final ToIntFunction<Long> compareByOrders = (this::getHotelOrders);
    private final Function<Long, LocalDate> compareByBuildDate = (h -> {
        Hotel hotel = findHotelByHotelId(h);
        return hotel.getBuildDate();
    });


    @Override
    public List<HotelCardDto> sortHotelsByScore(List<HotelCardDto> hotels) {

        List<Long> hotelIds = hotels.stream()
                .map(HotelCardDto::getHotelId)
                .sorted(Comparator
                        .comparingDouble(compareByScore).reversed()
                        .thenComparingInt(compareByOrders).reversed()
                        .thenComparing(compareByBuildDate).reversed())
                .limit(9)
                .peek(System.out::println) // 輸出排序的結果（可選）
                .toList();
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }


    @Override
    public List<HotelCardDto> sortHotelsByOrders(List<HotelCardDto> hotels) {

        List<Long> hotelIds = hotels.stream()
                .map(HotelCardDto::getHotelId)
                .sorted(Comparator
                        .comparingInt(compareByOrders).reversed()
                        .thenComparingDouble(compareByScore).reversed()
                        .thenComparing(compareByBuildDate).reversed())
                .limit(9)
                .toList();

        return findHotelCardDtoByIdFromList(hotels, hotelIds);

    }


    @Override
    public List<HotelCardDto> sortHotelsByBuildDate(List<HotelCardDto> hotels) {
        List<Long> hotelIds = hotels.stream()
                .map(HotelCardDto::getHotelId)
                .sorted(Comparator
                        .comparing(compareByBuildDate).reversed()
                        .thenComparingDouble(compareByScore).reversed()
                        .thenComparingInt(compareByOrders).reversed())
                .limit(9)
                .toList();
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }

    public List<HotelCardDto> findHotelCardDtoByIdFromList(List<HotelCardDto> hotels, List<Long> sortedIds) {
        //將id轉回hotelCard
        return sortedIds.stream().map(l ->
                        hotels.stream().filter(h -> h.getHotelId().equals(l))
                                .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Hotel updateHotelLikeList(Long userId, Long hotelId) {
        Hotel hotel = findHotelByHotelId(hotelId);
        if (hotel == null) {
            throw new RuntimeException("Hotel not found with id: " + hotelId);
        }
        List<Long> likedUsers = hotelRepository.getHotelFans(hotelId);
        if (likedUsers.contains(userId))
            likedUsers.remove(userId);
        else
            likedUsers.add(userId);
        hotel.setLikedByUsersIds(likedUsers);
        return hotelRepository.save(hotel);
    }


    @Override
    public Set<Integer> getHotelCity() {
        return hotelRepository.findAllCityAddress();
    }


}
