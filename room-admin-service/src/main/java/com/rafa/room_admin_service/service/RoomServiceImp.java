package com.rafa.room_admin_service.service;

import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImp implements RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Override
    public Room createRoom(Long hotelId, CreateRoomRequest request) {
        Room room = new Room();

        room.setRoomName(request.getRoomName());
        room.setSpecialties(request.getSpecialties());
        room.setCapacity(request.getCapacity());
        room.setRoomSize(request.getRoomSize());
        room.setHotelId(hotelId);
        List<Integer> prices = new ArrayList<>(request.getPrices());
        if (!prices.isEmpty())
            room.setPrices(prices);
        List<String> pictures = request.getRoomPic();
        room.setRoomPic(pictures);
        roomRepository.save(room);

        return room;
    }

    @Override
    public boolean deleteRoomByRoomId(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            roomRepository.deleteById(roomId);
            System.out.println("roomId" + roomId + "已成功刪除");
            return true;
        } else
            return false;
    }

    @Override
    public Long findHotelIdByRoomId(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("查無此房間"));
        return room.getHotelId();
    }

    @Override
    public List<Long> getRoomIdsByHotelId(Long hotelId) {
        return roomRepository.findRoomIdsByHotelId(hotelId);
    }

    @Override
    public List<Room> findRoomByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Override
    public List<String> findRoomNamesByHotelId(Long hotelId) {
        return roomRepository.findRoomNameByHotelId(hotelId);
    }

}
