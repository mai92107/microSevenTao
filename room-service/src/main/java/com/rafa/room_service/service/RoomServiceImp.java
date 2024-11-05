package com.rafa.room_service.service;

import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.CreateRoomRequest;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomServiceImp implements RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Override
    public List<Room> findRoomByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Override
    public Integer countPriceByLivingDate(Long roomId, List<LocalDate> dates) {
        Room room = roomRepository.findById(roomId).orElse(null);

        if (room == null || dates == null)
            return 0;
        System.out.println("計算價格的房間為 " + room.getRoomName());

        int totalPrice = 0;
        System.out.println("計算居住天數為 " + dates.size());

        if (!dates.isEmpty()) {
            List<Integer> priceList = dates.stream()
                    .map(d -> countByDate(room.getPrices(), d))
                    .toList();
            System.out.println("本次住宿每日價格表為" + priceList);
            totalPrice = priceList.parallelStream()
                    .mapToInt(p -> p)
                    .sum();
        }
        return totalPrice;
    }

    @Override
    public List<LocalDate> separateLivingDays(LocalDate start, LocalDate end) {
        if (start == null && end == null)
            return null;
        List<LocalDate> livingDays = new ArrayList<>();
        while (start.isBefore(end)) {
            livingDays.add(start);
            start = start.plusDays(1);
        }
        return livingDays;
    }

    @Override
    public Integer countByDate(List<Integer> roomPrices, LocalDate date) {
        if (roomPrices == null || roomPrices.isEmpty())
            return 0;
        System.out.println("價格是" + roomPrices);
        DayOfWeek chosenDayOfWeek = date.getDayOfWeek();
        System.out.println("查詢的星期：" + chosenDayOfWeek.getValue());
        System.out.println("查詢的星期：" + chosenDayOfWeek);
        return roomPrices.get(chosenDayOfWeek.getValue() - 1);
    }

    @Override
    public List<Long> filterInvalidRoomByDetails(List<Long> roomIds, Integer people) {
        List<Room> rooms = new ArrayList<>();
        for (Long id : roomIds) {
            Room room = roomRepository.findById(id).orElse(null);
            if (room != null)
                rooms.add(room);
        }
        return rooms.stream().filter(r -> r.getCapacity() >= people).map(Room::getRoomId).collect(Collectors.toList());
    }

    @Override
    public RoomCardDto convertRoomToRoomCard(Long roomId, LocalDate start, LocalDate end) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("找不到指定的房間: " + roomId));

        System.out.println("轉換這個room:" + roomId);
        List<String> roomPics = new ArrayList<>();
        List<String> specialties = new ArrayList<>();
        if (room.getRoomPic() != null)
            roomPics.addAll(room.getRoomPic());
        if (room.getSpecialties() != null)
            specialties.addAll(room.getSpecialties());
        int totalPrice = countPriceByLivingDate(room.getRoomId(), separateLivingDays(start, end));
        System.out.println("我的room照片數量" + room.getRoomPic().size());
        System.out.println("我的room設施數量" + room.getSpecialties().size());
        return new RoomCardDto(
                room.getRoomId(),
                roomPics,
                room.getRoomName(),
                specialties,
                totalPrice,
                room.getRoomSize(),
                room.getCapacity(),
                start,
                end
        );
    }


    @Override
    public List<Long> findRoomIdsByHotelId(Long hotelId) {
        return roomRepository.findRoomIdsByHotelId(hotelId);
    }

    @Override
    public List<String> findRoomNamesByRoomIds(List<Long> roomIds) {
        List<String> roomNames = new ArrayList<>();
        for (Long id : roomIds) {
            Room r = roomRepository.findById(id).orElse(null);
            if (r != null)
                roomNames.add(r.getRoomName());
        }

        return roomNames;
    }

    @Override
    public Integer getHotelMinPricePerDay(List<Long> roomIds, LocalDate start, LocalDate end) {
        return roomIds.stream()
                .mapToInt(id -> getMinPricePerDay(id, start, end))
                .min()
                .orElse(0);
    }

    @Override
    public Long findHotelIdByRoomId(Long roomId) {
        return roomRepository.findHotelIdByRoomId(roomId);
    }

    public Integer getMinPricePerDay(Long roomId, LocalDate start, LocalDate end) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("查無此房間: " + roomId));

        List<LocalDate> livingDates = separateLivingDays(start, end);

        return livingDates.stream()
                .mapToInt(date -> {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    List<Integer> prices = room.getPrices();

                    if (prices == null || prices.isEmpty()) {
                        return Integer.MAX_VALUE;
                    }

                    return prices.get(dayOfWeek.getValue() - 1);
                })
                .min()
                .orElseThrow(() -> new RuntimeException("找不到有效的日期價格"));
    }

}
