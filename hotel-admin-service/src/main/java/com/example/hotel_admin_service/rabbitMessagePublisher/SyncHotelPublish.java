package com.example.hotel_admin_service.rabbitMessagePublisher;

import com.example.hotel_admin_service.model.dto.CustomRabbitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncHotelPublish {

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final MessagePostProcessor afterSendMessage = message -> {
        log.info("消息已發送: {}", new String(message.getBody()));
        return message;
    };

    public void sendMsg(CustomRabbitMessage message) {
        rabbitTemplate.convertAndSend(message.getExchange(),message.getRoutingKey(), message.getMessage(), afterSendMessage);
    }


}
