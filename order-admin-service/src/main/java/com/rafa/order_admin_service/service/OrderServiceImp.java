package com.rafa.order_admin_service.service;

import com.rafa.order_admin_service.model.Orders;
import com.rafa.order_admin_service.model.STATUS;
import com.rafa.order_admin_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Override
    public List<Orders> findByRoomId(Long roomId) {
        return orderRepository.findByRoomId(roomId);
    }


    @Override
    public List<Orders> getOrdersByRoomList(List<Long> roomIds) {
        List<Orders> allOrders = new ArrayList<>();
        orderRepository.updateExpiredOrders();
        for (Long roomId : roomIds) {
            allOrders.addAll(findByRoomId(roomId));
        }
        return allOrders;
    }

    @Override
    public List<List<Orders>> seperateStatus(List<Orders> allOrders) {
        List<List<Orders>> allOrder = new ArrayList<>();
        List<Orders> validOrders = allOrders.stream().filter(o -> o.getOrderStatus() == STATUS.VALID).toList();
        List<Orders> pendingOrders = allOrders.stream().filter(o -> o.getOrderStatus() == STATUS.PENDING).toList();
        List<Orders> disannulOrders = allOrders.stream().filter(o -> o.getOrderStatus() == STATUS.DISANNUL).toList();
        List<Orders> finishedOrders = allOrders.stream().filter(o -> o.getOrderStatus() == STATUS.FINISHED).toList();
        List<Orders> canceledOrders = allOrders.stream().filter(o -> o.getOrderStatus() == STATUS.CANCELED).toList();
        allOrder.add(validOrders);
        allOrder.add(pendingOrders);
        allOrder.add(disannulOrders);
        allOrder.add(finishedOrders);
        allOrder.add(canceledOrders);
        return allOrder;
    }

    @Override
    public Orders acceptOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單id" + orderId));
        System.out.println(STATUS.VALID);
        order.setOrderStatus(STATUS.VALID);
        return orderRepository.save(order);
    }

    @Override
    public Orders acceptCancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單id" + orderId));
        System.out.println(STATUS.CANCELED);
        order.setOrderStatus(STATUS.CANCELED);
        return orderRepository.save(order);
    }
}
