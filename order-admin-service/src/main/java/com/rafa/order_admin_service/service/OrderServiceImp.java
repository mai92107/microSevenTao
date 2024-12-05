package com.rafa.order_admin_service.service;

import com.rafa.order_admin_service.model.CustomRabbitMessage;
import com.rafa.order_admin_service.model.Orders;
import com.rafa.order_admin_service.model.STATUS;
import com.rafa.order_admin_service.rabbitMessagePublisher.SyncOrderPublish;
import com.rafa.order_admin_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImp implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    SyncOrderPublish syncOrderPublish;

    @Override
    public List<Orders> findByRoomId(Long roomId) {
        return orderRepository.findByRoomId(roomId);
    }

    @Override
    public List<Orders> getOrdersByRoomLists(List<Long> roomIds) {
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

    @Transactional
    @Override
    public Orders acceptOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單id" + orderId));
        System.out.println(STATUS.VALID);
        order.setOrderStatus(STATUS.VALID);
        Orders newOrder = orderRepository.save(order);
        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("adminUpdateOrderRoute", "orderExchange", newOrder));
        } catch (Exception e) {
            log.error("(acceptOrder) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return newOrder;
    }

    @Transactional
    @Override
    public Orders acceptCancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("查無此訂單id" + orderId));
        System.out.println(STATUS.CANCELED);
        order.setOrderStatus(STATUS.CANCELED);
        Orders newOrder = orderRepository.save(order);

        try {
            syncOrderPublish.sendMsg(new CustomRabbitMessage("adminUpdateOrderRoute", "orderExchange", newOrder));
        } catch (Exception e) {
            log.error("(acceptCancelOrder) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
        }
        return newOrder;
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "userUpdateOrderQueue", durable="true"))
    public void updateOrderStatus(Orders order) {
        Orders newOrder = orderRepository.save(order);
        log.info("(wantCancelOrder)訂單{}更新成功", newOrder.getId());
    }

    @RabbitListener(queuesToDeclare = @Queue (name = "createOrderQueue", durable="true"))
    public void createOrder(Orders order) {
        Orders newOrder = orderRepository.save(order);
        log.info("(createOrder)訂單{}新增成功", newOrder.getId());
    }
}
