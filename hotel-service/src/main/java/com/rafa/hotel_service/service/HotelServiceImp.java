package com.rafa.hotel_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.hotel_service.exception.HotelNotFoundException;
import com.rafa.hotel_service.exception.SearchDataErrorException;
import com.rafa.hotel_service.feign.OrderInterface;
import com.rafa.hotel_service.feign.RoomInterface;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.CheckRoomAvailableByOrder;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.repository.HotelRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.*;
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

    @Autowired
    CacheManager cacheManager;


    private BoundHashOperations<String, String, Hotel> getHotelBound() {
        BoundHashOperations<String, String, Hotel> hb = redisTemplate.boundHashOps("hotel");
        hb.expire(Duration.ofDays(1));
        return hb;
    }


    @Transactional
    private Optional<Hotel> findHotelByHotelId(Long hotelId) {
        Hotel hotel = getHotelBound().get(hotelId + "");
        if (hotel != null) {
            log.info("(findHotelByHotelId)Redis中找到此hotel" + hotelId);
            return Optional.of(hotel);
        }

        Hotel findHotel = hotelRepository.findById(hotelId).orElse(null);
        if (findHotel == null)
            return Optional.empty();
        log.info("(findHotelByHotelId)資料庫中找到hotel，開始寫入redis");
        getHotelBound().put(hotelId + "", findHotel);
        return Optional.of(findHotel);
    }

    private HotelCardDto convertHotelToHotelCardDto(
            Hotel hotel, LocalDate start, LocalDate end, Integer people) {

        HotelCardDto hc = new HotelCardDto();
        hc.setScore(hotel.getScore());
        hc.setBossId(hotel.getBossId());
        hc.setChName(hotel.getChName());
        hc.setEnName(hotel.getEnName());
        hc.setIntroduction(hotel.getIntroduction());
        hc.setHotelId(hotel.getHotelId());
        if (!hotel.getPictures().isEmpty())
            hc.setPicture(hotel.getPictures().get(0));
        List<Long> validRoomIds =
                roomInterface.findRoomIdsByHotelId(hotel.getHotelId()).getBody().getData();
        log.info("(convertHotelToHotelCardDto)篩選前房間總數為" + validRoomIds.size());

        if (!validRoomIds.isEmpty() && people != null) {
            validRoomIds = roomInterface.filterValidRoomBySize(validRoomIds, people).getBody().getData();
            log.info("(convertHotelToHotelCardDto)依人數篩選後房間總數為" + validRoomIds.size());
        }
        Integer minPrice = null;
        if (validRoomIds.isEmpty())
            return null;
        if (start != null && end != null) {
            validRoomIds = orderInterface.checkHotelAvailableRooms(new CheckRoomAvailableByOrder(validRoomIds, start, end)).getBody().getData();
            log.info("(convertHotelToHotelCardDto)依時間篩選後房間總數為" + validRoomIds.size());
        }
        if (validRoomIds.isEmpty())
            return null;
        if(start!=null&&end!=null) {
            minPrice = roomInterface.getMinPricePerDay(validRoomIds, start.toString(), end.toString()).getBody().getData();
            log.info("(convertHotelToHotelCardDto)取旅店最小金額為：" + minPrice);
            hc.setMinPrice(minPrice);
        }

        List<String> roomNames = roomInterface.findRoomNamesByRoomIds(validRoomIds).getBody().getData();

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
            getHotelBound().putAll(hotelMap);
        }
        hotels = hotels.parallelStream().filter(h ->
                !Objects.requireNonNull(roomInterface.findRoomIdsByHotelId(h.getHotelId()).getBody()).getData().isEmpty()).toList();
        log.info("(findALLHotelsByDetail)搜尋符合的有幾間：" + hotels.size());
        return convertHotelsToHotelCardDtos(hotels, start, end, people);
    }

    @Override
    public HotelDetailDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException {
        //hotel頁面(未經條件轉換)
        Hotel hotel = findHotelByHotelId(hotelId).orElse(null);
        if (hotel == null)
            throw new HotelNotFoundException(hotelId);
        return objectMapper.convertValue(hotel, HotelDetailDto.class);
    }

    private List<Hotel> getAllHotels() {

        List<Hotel> hotels = getHotelBound().values();
        if (hotels != null && !hotels.isEmpty() && hotels.size() > 5) {
            log.info("(getAllHotels)自redis取到的資料" + hotels.size());
            return hotels;
        }
        hotels = hotelRepository.findAll();
        log.info("(getAllHotels)自資料庫取到的資料" + hotels.size());
        Map<String, Hotel> hotelMap = new HashMap<>();
        hotels.forEach(h -> hotelMap.put(h.getHotelId() + "", h));
        getHotelBound().putAll(hotelMap);
        return hotels;
    }


    @Override
    public List<HotelCardDto> getFavoriteHotelsByUserId(Long userId) {
        //個人頁面我的最愛
        Set<String> favoriteHotels = favoriteHotels(userId);
        if(favoriteHotels.isEmpty())
            return new ArrayList<>();
        List<Hotel> hotels = favoriteHotels.parallelStream().map(id -> findHotelByHotelId(Long.parseLong(id)).get()).toList();
        return convertHotelsToHotelCardDtos(hotels, null, null, null);
    }

    private Set<String> favoriteHotels(Long userId) {
        Cache favorCache = cacheManager.getCache("favorHotel");
        Cache.ValueWrapper wrapper = favorCache.get(userId + "");
        if (wrapper != null && wrapper.get() != null) {
            return (Set<String>) wrapper.get();
        }
        Set<String> favorHotel = hotelRepository.findFavoriteHotelIdsByUserId(userId);
        cacheManager.getCache("favorHotel").put(userId, favorHotel);
        return favorHotel;
    }

    @Override
    public Boolean checkIsFavorite(Long userId, Long hotelId) {
        Set<String> favoriteHotels = favoriteHotels(userId);
        return favoriteHotels.stream().anyMatch(id -> Long.parseLong(id) == (hotelId));
    }


    @Override
    public HotelEntity searchAllHotelsWithSortedMethods(Integer cityCode, String keyword, LocalDate start, LocalDate end, Integer people) throws SearchDataErrorException {

        List<HotelCardDto> filteredHotel = findALLHotelsByDetail(cityCode,
                keyword, start, end, people);
        HotelEntity allHotelType = new HotelEntity(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>());

        if (filteredHotel.isEmpty())
            return allHotelType;
        log.info("(searchAllHotelsWithSortedMethods)總共找到" + filteredHotel.size() + "間旅店，開始排序");
        allHotelType.setHotels(filteredHotel);
        allHotelType.setBestHotels(sortHotelsByScore(filteredHotel));
        allHotelType.setHotHotels(sortHotelsByOrders(filteredHotel));
        allHotelType.setNewHotels(sortHotelsByBuildDate(filteredHotel));

        return allHotelType;
    }


    private Integer getHotelOrders(Long hotelId) {
        //計算首頁排序
        List<Long> roomIds = roomInterface.findRoomIdsByHotelId(hotelId).getBody().getData();
        if (roomIds == null || roomIds.isEmpty())
            return 0;
        Integer hotelOrders = orderInterface.getHotelOrderCount(roomIds).getBody().getData();
        if (hotelOrders == null)
            return 0;
        return hotelOrders;
    }

    private Comparator<Long> getCompareByScore() {
        return Comparator.comparingDouble(h ->
                {
                    try {
                        return findHotelByHotelId(h).orElseThrow(() -> new HotelNotFoundException(h)).getScore();
                    } catch (HotelNotFoundException e) {
                        log.error("(compareByScore)" + e.getMsg());
                        return 0;
                    }
                }
        );
    }

    private Comparator<Long> getCompareByOrders() {
        return Comparator.comparingInt(this::getHotelOrders);
    }

    private Comparator<Long> getToCompareByBuildDate() {
        return Comparator.comparing(h ->
                {
                    try {
                        return findHotelByHotelId(h).orElseThrow(() -> new HotelNotFoundException(h))
                                .getBuildDate();
                    } catch (HotelNotFoundException e) {
                        log.error("(compareByBuildDate)" + e.getMsg());
                        return LocalDateTime.MIN;
                    }
                }
        );
    }

    private Comparator<Long> multiCompareByScore() {
        log.info("(multiCompareByScore)按分數排序");
        return getCompareByScore().reversed()
                .thenComparing(getCompareByOrders()).reversed()
                .thenComparing(getToCompareByBuildDate()).reversed();
    }

    private Comparator<Long> multiCompareByOrder() {
        return getCompareByOrders().reversed()
                .thenComparing(getCompareByScore()).reversed()
                .thenComparing(getToCompareByBuildDate()).reversed();
    }

    private Comparator<Long> multiCompareByBuildDate() {
        return getToCompareByBuildDate().reversed()
                .thenComparing(getCompareByScore()).reversed()
                .thenComparing(getCompareByOrders()).reversed();
    }

    private List<HotelCardDto> sortHotelsByScore(List<HotelCardDto> hotels) {
        log.info("(sortHotelsByScore)收到排序數量" + hotels.size());
        List<Long> hotelIds = new ArrayList<>(hotels.parallelStream().mapToLong(HotelCardDto::getHotelId).boxed().toList());
        log.info("(sortHotelsByScore)hotelId排序數量" + hotelIds.size());
        hotelIds.sort(multiCompareByScore());
        log.info("(sortHotelsByScore)hotelId排序後數量" + hotelIds.size());
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }

    private List<HotelCardDto> sortHotelsByOrders(List<HotelCardDto> hotels) {
        log.info("(sortHotelsByOrders)收到排序數量" + hotels.size());
        List<Long> hotelIds = new ArrayList<>(hotels.parallelStream().mapToLong(HotelCardDto::getHotelId).boxed().toList());
        hotelIds.sort(multiCompareByOrder());
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }

    private List<HotelCardDto> sortHotelsByBuildDate(List<HotelCardDto> hotels) {
        log.info("(sortHotelsByBuildDate)收到排序數量" + hotels.size());
        List<Long> hotelIds = new ArrayList<>(hotels.parallelStream().mapToLong(HotelCardDto::getHotelId).boxed().toList());
        hotelIds.sort(multiCompareByBuildDate());
        return findHotelCardDtoByIdFromList(hotels, hotelIds);
    }

    private List<HotelCardDto> findHotelCardDtoByIdFromList(List<HotelCardDto> hotels, List<Long> sortedIds) {
        //將id轉回hotelCard
        return sortedIds.stream().map(l ->
                        hotels.parallelStream().filter(h -> h.getHotelId().equals(l))
                                .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Hotel updateHotelLikeList(Long userId, Long hotelId) throws HotelNotFoundException {
        Hotel hotel = findHotelByHotelId(hotelId).orElse(null);
        if (hotel == null)
            throw new HotelNotFoundException(hotelId);
        List<Long> likedUsers = hotelRepository.getHotelFans(hotelId);
        if (likedUsers.contains(userId)) {
            likedUsers.remove(userId);
            cacheManager.getCache("favorHotel").evict(userId);
        } else {
            likedUsers.add(userId);
            cacheManager.getCache("favorHotel").evict(userId);
        }
        hotel.setLikedByUsersIds(likedUsers);
        ;
        return hotelRepository.save(hotel);
    }


    @Cacheable(value = "cities", key = "'cities'")
    @Override
    public Set<String> getHotelCity() {
        Set<String> allCities = hotelRepository.findAllCityAddress();
        return allCities;
    }

    @RabbitListener(queuesToDeclare = @Queue (name = "createHotelQueue", durable="true"))
    public void createHotel(Hotel hotel) {
        Hotel newHotel = hotelRepository.save(hotel);
        log.info("(createHotel)儲存旅店成功數量" + newHotel.getHotelId());
    }

    @RabbitListener(queuesToDeclare = @Queue (name = "deleteHotelQueue", durable="true"))
    public void deleteHotel(Long hotelId) {
        hotelRepository.deleteById(hotelId);
        log.info("(deleteHotel)刪除旅店成功" + hotelId);
    }

    @RabbitListener(queuesToDeclare = @Queue (name = "updateHotelQueue", durable="true"))
    public void updateHotel(Hotel hotel) {
        hotelRepository.save(hotel);
        log.info("(updateHotel)旅店更新成功" + hotel.getHotelId());
    }

    @RabbitListener(queuesToDeclare = @Queue (name = "updateUserHotelScoreQueue", durable="true"))
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
        getHotelBound().put(hotelId+"",newHotel);
        log.info("(updateHotelScore)旅店{}分數{}更新成功", hotelId, score);
    }

}
