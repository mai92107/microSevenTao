package com.rafa.order_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.SEX;
import com.rafa.order_service.model.STATUS;
import com.rafa.order_service.model.dto.CustomRabbitMessage;
import com.rafa.order_service.model.dto.OrderDto;
import com.rafa.order_service.model.dto.UserDto;
import com.rafa.order_service.rabbitMessagePublisher.SyncOrderPublish;
import com.rafa.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class OrderServiceImp implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SyncOrderPublish syncOrderPublish;

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate start, LocalDate end) {
        List<Orders> roomOrders = orderRepository.findByRoomId(roomId);
        return roomOrders.stream().filter(o -> o.getOrderStatus().equals(STATUS.VALID) || o.getOrderStatus().equals(STATUS.DISANNUL))
                .noneMatch(o -> o.getCheckInDate().isBefore(end) && o.getCheckOutDate().isAfter(start));
    }


    @Override
    public List<OrderDto> getUserFinishedOrder(Long userId) {
        orderRepository.updateValidExpiredOrders();
        System.out.println("找使用者" + userId + "訂單有幾筆" + orderRepository.findByUserId(userId).size());
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(o -> o.getOrderStatus() == STATUS.FINISHED).toList();
        return orders.stream().map(o -> objectMapper.convertValue(o, OrderDto.class)).toList();
    }

    @Override
    public List<OrderDto> getUserCanceledOrder(Long userId) {
        orderRepository.updateInvalidExpiredOrders();
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

    @Transactional
    @Override
    public boolean deleteInvalidOrderFromUser(Long userId, long orderId) {
        Optional<Orders> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            System.out.println("此訂單是空的");
            return false;
        }
        Orders existOrder = order.get();
        if (!Objects.equals(existOrder.getUserId(), userId)) {
            System.out.println("無法刪除其他人訂單");
            return false;
        }
        if (existOrder.getOrderStatus() != STATUS.FINISHED && existOrder.getOrderStatus() != STATUS.CANCELED) {
            System.out.println("訂單未完成，無法刪除");
            return false;
        }
        order.get().setUserId(null);
        Orders newOrder = orderRepository.save(order.get());

        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("userUpdateOrderRoute", "orderExchange", newOrder));
        } catch (Exception e) {
            log.error("(deleteRoomsByRoomIds) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
            return false;
        }
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

    @Transactional
    @Override
    public Orders wantCancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單" + orderId));
        order.setOrderStatus(STATUS.DISANNUL);

        Orders newOrder = orderRepository.save(order);
        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("userUpdateOrderRoute", "orderExchange", newOrder));
        } catch (Exception e) {
            log.error("(wantCancelOrder) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return newOrder;
    }

    @Override
    public Orders findOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).get();
    }

    @Override
    public void updateOrderCommentStatus(Orders order, Boolean status) {
        order.setCommented(status);
        orderRepository.save(order);
    }


    @Transactional
    @Override
    public Orders cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單" + orderId));
        order.setOrderStatus(STATUS.CANCELED);
        Orders newOrder = orderRepository.save(order);

        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("userUpdateOrderRoute", "orderExchange", newOrder));
        } catch (Exception e) {
            log.error("(cancelOrder) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return newOrder;
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

        Orders orders = orderRepository.save(newOrder);
        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("createOrderRoute", "orderExchange", orders));
        } catch (Exception e) {
            log.error("(createOrder) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return orders;
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "adminUpdateOrderQueue", durable="true"))
    public void updateOrderStatus(Orders order) {
        Orders newOrder = orderRepository.save(order);
        log.info("(acceptOrder)訂單{}更新成功", newOrder.getId());
    }

}
