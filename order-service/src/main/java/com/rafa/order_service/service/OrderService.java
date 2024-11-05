package com.rafa.order_service.service;


import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.dto.UserDto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface OrderService {

    public Orders createOrder(UserDto user, Long roomId, Integer roomPrice, String roomName, String roomPic, LocalDate start, LocalDate end);

    public boolean isRoomAvailable(Long roomId, LocalDate start, LocalDate end);

    public List<Orders> getUserFinishedOrder(Long userId);
    public List<Orders> getUserCanceledOrder(Long userId);
    public List<Orders> getUserPendingOrder(Long userId);
    public List<Orders> getUserDisannulOrder(Long userId);

    public List<Orders> getUserValidOrder(Long userId);

    public boolean deleteInvalidOrderFromUser(Long userId, long orderId);

    public List<Long> filterHotelUnavailableRoom(List<Long> roomIds, LocalDate start, LocalDate end);

    public Integer getOrderCountByRoomList(List<Long> roomIds);

    public Orders cancelOrder(Long orderId);

    public Orders wantCancelOrder(Long orderId);
}
