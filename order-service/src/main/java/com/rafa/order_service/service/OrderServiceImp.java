package com.rafa.order_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.SEX;
import com.rafa.order_service.model.STATUS;
import com.rafa.order_service.model.dto.OrderDto;
import com.rafa.order_service.model.dto.UserDto;
import com.rafa.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate start, LocalDate end) {
        List<Orders> roomOrders = orderRepository.findByRoomId(roomId);
        return roomOrders.stream().filter(o -> o.getOrderStatus().equals(STATUS.VALID) || o.getOrderStatus().equals(STATUS.DISANNUL))
                .noneMatch(o -> !o.getCheckInDate().isAfter(end) && !o.getCheckOutDate().isBefore(start));
    }


    @Override
    public List<OrderDto> getUserFinishedOrder(Long userId) {
        orderRepository.updateExpiredOrders();
        System.out.println("找使用者" + userId + "訂單有幾筆" + orderRepository.findByUserId(userId).size());
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.FINISHED).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();
    }

    @Override
    public List<OrderDto> getUserCanceledOrder(Long userId) {
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.CANCELED).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();
    }

    @Override
    public List<OrderDto> getUserPendingOrder(Long userId) {
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.PENDING).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();
    }

    @Override
    public List<OrderDto> getUserDisannulOrder(Long userId) {
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.DISANNUL).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();

    }

    @Override
    public List<OrderDto> getUserValidOrder(Long userId) {
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.VALID).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();

    }

    @Override
    public boolean deleteInvalidOrderFromUser(Long userId, long orderId) {
        Optional<Orders> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            System.out.println("此訂單是空的");
            return false;
        }
        if (!Objects.equals(order.get().getUserId(), userId)) {
            System.out.println("無法刪除其他人訂單");
            return false;
        }
        if (order.get().getCheckOutDate().isAfter(LocalDate.now())) {
            System.out.println("訂單未完成，無法刪除");
            return false;
        }
        order.get().setUserId(null);
        orderRepository.save(order.get());
        return true;
    }

    @Override
    public List<Long> filterHotelUnavailableRoom(List<Long> roomIds, LocalDate start, LocalDate end) {
        return roomIds.stream().filter((a) -> isRoomAvailable(a, start, end)).toList();
    }

    @Override
    public Integer getOrderCountByRoomList(List<Long> roomIds) {
        return (int) roomIds.stream().map(id -> orderRepository.findByRoomId(id)).mapToLong(List::size).sum();
    }

    @Override
    public Orders wantCancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單" + orderId));
        order.setOrderStatus(STATUS.DISANNUL);
        return orderRepository.save(order);
    }

    @Override
    public Orders findOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).get();
    }

    @Override
    public Boolean updateOrderCommentStatus(Long userId, Long orderId, Boolean status) {
        Orders orders = orderRepository.findById(orderId).orElseGet(null);
        if (orders == null || !Objects.equals(orders.getUserId(), userId))
            return false;
        orders.setCommented(status);
        orderRepository.save(orders);
        return true;
    }

    @Override
    public Orders cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單" + orderId));
        order.setOrderStatus(STATUS.CANCELED);
        return orderRepository.save(order);
    }

    @Override
    public Orders createOrder(UserDto user, Long roomId, Integer roomPrice, String roomName, String roomPic, LocalDate start, LocalDate end) {
        System.out.println("我的order前入住時間是：" + start + "離開時間是：" + end);
        Orders newOrder = new Orders();
        newOrder.setUserId(user.getUserId());
        String name = user.getLastName();
        if (user.getFirstName() != null)
            name += user.getFirstName();
        if (user.getSex() != null) {
            if (user.getSex().equals(SEX.MALE)) {
                name += "先生";
            } else if (user.getSex().equals(SEX.FEMALE)) {
                name += "小姐";
            }
        }
        newOrder.setName(name);
        newOrder.setPhoneNum(user.getPhoneNum());
        newOrder.setCheckInDate(start);
        newOrder.setCheckOutDate(end);
        newOrder.setRoomName(roomName);
        newOrder.setRoomPic(roomPic);
        newOrder.setTotalPrice(roomPrice);
        newOrder.setRoomId(roomId);
        System.out.println("我的order後入住時間是：" + newOrder.getCheckInDate() + "離開時間是：" + newOrder.getCheckOutDate());
        return orderRepository.save(newOrder);
    }
}
