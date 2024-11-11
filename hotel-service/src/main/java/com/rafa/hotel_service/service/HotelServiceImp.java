package com.rafa.hotel_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.hotel_service.exception.HotelNotFoundException;
import com.rafa.hotel_service.exception.SearchDataErrorException;
import com.rafa.hotel_service.feign.OrderInterface;
import com.rafa.hotel_service.feign.RoomInterface;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.repository.HotelRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

@Service
@Slf4j
public class HotelServiceImp implements HotelService {

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    RoomInterface roomInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisTemplate<String, Hotel> redisTemplate;

    @Transactional
    private Optional<Hotel> findHotelByHotelId(Long hotelId) {
        Hotel hotel = (Hotel) redisTemplate.opsForHash().get("hotel", hotelId + "");
        if (hotel != null) {
            log.info("(findHotelByHotelId)Redis中找到此hotel" + hotelId);
            return Optional.of(hotel);
        }

        Hotel findHotel = hotelRepository.findById(hotelId).orElse(null);
        if (findHotel == null)
            return Optional.empty();
        log.info("(findHotelByHotelId)資料庫中找到hotel，開始寫入redis");
        redisTemplate.opsForHash().put("hotel", hotelId + "", findHotel);
        return Optional.of(findHotel);
    }

    private HotelCardDto convertHotelToHotelCardDto(
            Hotel hotel, LocalDate start, LocalDate end, Integer people) {

        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        if (!hotel.getPictures().isEmpty())
            hc.setPicture(hotel.getPictures().get(0));
        List<Long> validRoomIds =
                roomInterface.findRoomIdsByHotelId(hotel.getHotelId()).getBody();
        log.info("(convertHotelToHotelCardDto)篩選前房間總數為" + validRoomIds.size());

        if (!validRoomIds.isEmpty() && people != null) {
            validRoomIds = roomInterface.filterValidRoomBySize(validRoomIds, people).getBody();
            log.info("(convertHotelToHotelCardDto)依人數篩選後房間總數為" + validRoomIds.size());
        }
        Integer minPrice = null;
        if (!validRoomIds.isEmpty() && start != null && end != null) {
            minPrice = roomInterface.getMinPricePerDay(validRoomIds, start, end).getBody();
        }
        if (validRoomIds.isEmpty())
            return null;
        log.info("(convertHotelToHotelCardDto)取旅店最小金額為：" + minPrice);
        hc.setMinPrice(minPrice);

        List<String> roomNames = roomInterface.findRoomNamesByRoomIds(validRoomIds).getBody();

        if (roomNames == null || roomNames.isEmpty()) {
            log.info("(convertHotelToHotelCardDto)查無房間符合");
            return null;
        }
        hc.setRoomName(roomNames);
        return hc;
    }


    private List<HotelCardDto> convertHotelsToHotelCardDtos(List<Hotel> hotels, LocalDate start, LocalDate end, Integer people) {
        List<HotelCardDto> hotelCards = new ArrayList<>();
        for (Hotel h : hotels) {
            log.info("(convertHotelsToHotelCardDtos)這間旅店開始轉換CardDto：" + h.getChName());
            HotelCardDto hcDto = convertHotelToHotelCardDto(h, start, end, people);
            if (hcDto != null)
                hotelCards.add(hcDto);
        }
        log.info("(convertHotelsToHotelCardDtos)經轉換後旅店數量" + hotelCards.size());
        return hotelCards;
    }

    private List<HotelCardDto> findALLHotelsByDetail(Integer cityCode, String keyword,
                                                     LocalDate start, LocalDate end, Integer people)
            throws SearchDataErrorException {
        //首頁用 可適用條件為空值（初始狀態）
        log.info("(findALLHotelsByDetail)搜尋條件為：城市碼：" + cityCode + "，關鍵字為：" + keyword + "，開始時間：" + start + "，結束時間：" + end + "，人數：" + people);
        List<Hotel> hotels;
        if ((start != null && start.isBefore(LocalDate.now())) ||
                (end != null && !end.isAfter(start)) ||
                (people != null && people <= 0)) {
            log.error("(findALLHotelsByDetail)錯誤搜尋資料，請重新嘗試");
            throw new SearchDataErrorException(cityCode, keyword, start, end, people);
        }
        if (cityCode == null && keyword == null) {
            hotels = getAllHotels();
        } else {
            hotels = hotelRepository.findHotelsByDetail(cityCode, keyword);
            log.info("(findALLHotelsByDetail)自資料庫取到的資料" + hotels.size());
            Map<String, Hotel> hotelMap = new HashMap<>();
            hotels.forEach(h -> hotelMap.put(h.getHotelId() + "", h));
            redisTemplate.opsForHash().putAll("hotel", hotelMap);
        }
        hotels = hotels.stream().filter(h ->
                !Objects.requireNonNull(roomInterface.findRoomIdsByHotelId(h.getHotelId()).getBody()).isEmpty()).toList();
        log.info("(findALLHotelsByDetail)搜尋符合的有幾間：" + hotels.size());
        return convertHotelsToHotelCardDtos(hotels, start, end, people);
    }

    @Override
    public HotelDetailDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException {
        //hotel頁面(未經條件轉換)
        Hotel hotel = findHotelByHotelId(hotelId).orElse(null);
        if (hotel==null)
            throw new HotelNotFoundException(hotelId);
        return objectMapper.convertValue(hotel, HotelDetailDto.class);
    }

    private List<Hotel> getAllHotels() {

        List<Hotel> hotels = redisTemplate.opsForHash()
                .values("hotel").stream().map(o -> (Hotel) o).toList();
        if (!hotels.isEmpty() && hotels.size() > 5) {
            log.info("(getAllHotels)自redis取到的資料" + hotels.size());
            return hotels;
        }
        hotels = hotelRepository.findAll();
        log.info("(getAllHotels)自資料庫取到的資料" + hotels.size());
        Map<String, Hotel> hotelMap = new HashMap<>();
        hotels.forEach(h -> hotelMap.put(h.getHotelId() + "", h));
        redisTemplate.opsForHash().putAll("hotel", hotelMap);
        return hotels;
    }


    @Override
    public List<HotelCardDto> getFavoriteHotelsByUserId(Long userId) {
        //個人頁面我的最愛
        List<Hotel> favoriteHotels = favoriteHotels(userId);
        return convertHotelsToHotelCardDtos(favoriteHotels, null, null, null);
    }

    @Cacheable(value = "favorHotels", key = "#userId")
    private List<Hotel> favoriteHotels(Long userId) {
        return hotelRepository.findFavoriteHotelsByUserId(userId);
    }

    @Override
    public Boolean checkIsFavorite(Long userId, Long hotelId) {
        List<Hotel> favoriteHotels = favoriteHotels(userId);
        return !favoriteHotels.stream().noneMatch(h -> Objects.equals(h.getHotelId(), hotelId));
    }


    @Override
    public HotelEntity searchAllHotelsWithSortedMethods(Integer cityCode, String keyword, LocalDate start, LocalDate end, Integer people) throws SearchDataErrorException {

        List<HotelCardDto> filteredHotel = findALLHotelsByDetail(cityCode,
                keyword, start, end, people);
        log.info("(searchAllHotelsWithSortedMethods)總共找到" + filteredHotel.size() + "間旅店，開始排序");
        HotelEntity allHotelType = new HotelEntity();
        allHotelType.setHotels(filteredHotel);
        allHotelType.setBestHotels(sortHotelsByScore(filteredHotel));
        allHotelType.setHotHotels(sortHotelsByOrders(filteredHotel));
        allHotelType.setNewHotels(sortHotelsByBuildDate(filteredHotel));


        return allHotelType;
    }


    private Integer getHotelOrders(Long hotelId) {
        //計算首頁排序
        List<Long> roomIds = roomInterface.findRoomIdsByHotelId(hotelId).getBody();
        Integer hotelOrders = orderInterface.getHotelOrderCount(roomIds).getBody();
        if (hotelOrders == null)
            return 0;
        return hotelOrders;
    }


    private final ToDoubleFunction<Long> compareByScore = (h ->
    {
        try {
            return findHotelByHotelId(h).orElseThrow(() -> new HotelNotFoundException(h)).getScore();
        } catch (HotelNotFoundException e) {
            log.error("(compareByScore)" + e.getMsg());
            return 0;
        }
    }
    );
    private final ToIntFunction<Long> compareByOrders = (this::getHotelOrders);
    private final Function<Long, LocalDateTime> compareByBuildDate = (h ->
    {
        try {
            return findHotelByHotelId(h).orElseThrow(() -> new HotelNotFoundException(h)).getBuildDate();
        } catch (HotelNotFoundException e) {
            log.error("(compareByBuildDate)" + e.getMsg());
            return null;
        }
    }
    );


    private List<HotelCardDto> sortHotelsByScore(List<HotelCardDto> hotels) {

        List<Long> hotelIds = hotels.stream()
                .map(HotelCardDto::getHotelId)
                .sorted(Comparator
                        .comparingDouble(compareByScore).reversed()
                        .thenComparingInt(compareByOrders).reversed()
                        .thenComparing(compareByBuildDate).reversed())
                .limit(9)
                .toList();
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }


    private List<HotelCardDto> sortHotelsByOrders(List<HotelCardDto> hotels) {

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


    private List<HotelCardDto> sortHotelsByBuildDate(List<HotelCardDto> hotels) {
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

    private List<HotelCardDto> findHotelCardDtoByIdFromList(List<HotelCardDto> hotels, List<Long> sortedIds) {
        //將id轉回hotelCard
        return sortedIds.stream().map(l ->
                        hotels.stream().filter(h -> h.getHotelId().equals(l))
                                .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @CachePut(value = "favorHotels", key = "#userId")
    @Override
    public Hotel updateHotelLikeList(Long userId, Long hotelId) throws HotelNotFoundException {
        Hotel hotel = findHotelByHotelId(hotelId).orElse(null);
        if (hotel == null)
            throw new HotelNotFoundException(hotelId);
        List<Long> likedUsers = hotelRepository.getHotelFans(hotelId);
        if (likedUsers.contains(userId))
            likedUsers.remove(userId);
        else
            likedUsers.add(userId);
        hotel.setLikedByUsersIds(likedUsers);
        ;
        return hotelRepository.save(hotel);
    }


    @Cacheable(value = "city")
    @Override
    public Set<Integer> getHotelCity() {
        return hotelRepository.findAllCityAddress();
    }


}
