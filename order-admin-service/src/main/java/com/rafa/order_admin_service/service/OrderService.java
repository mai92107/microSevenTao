package com.rafa.order_admin_service.service;

import com.rafa.order_admin_service.model.Orders;

import java.util.List;

public interface OrderService {
    public List<Orders> findByRoomId(Long roomId);

    public List<Orders> getOrdersByRoomLists(List<Long> roomIds);

    public List<List<Orders>> seperateStatus(List<Orders> allOrders);

    public Orders acceptOrder(Long orderId);

    public Orders acceptCancelOrder(Long orderId);
}
