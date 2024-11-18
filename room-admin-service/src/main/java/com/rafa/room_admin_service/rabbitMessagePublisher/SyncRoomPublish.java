package com.rafa.room_admin_service.rabbitMessagePublisher;

import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.CustomRabbitMessage;
import com.rafa.room_admin_service.model.dto.RoomDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Slf4j
public class SyncRoomPublish {

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
