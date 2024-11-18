package com.rafa.room_admin_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {


    @Bean
    public Queue syncCreateRoomQueue(){
        return new Queue("createRoomQueue",true,false,false);
    }

    @Bean
    public Queue syncDeleteRoomQueue(){
        return new Queue("deleteRoomQueue",true,false,false);
    }

    @Bean
    public DirectExchange roomExchange(){
        return new DirectExchange("roomExchange");
    }

    @Bean
    public Binding createRoomBinding(){
        return BindingBuilder.bind(syncCreateRoomQueue()).to(roomExchange()).with("createRoomRoute");
    }

    @Bean
    public Binding deleteRoomBinding(){
        return BindingBuilder.bind(syncDeleteRoomQueue()).to(roomExchange()).with("deleteRoomRoute");
    }

    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setMessageConverter(converter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息已成功發送到交換機");
            } else {
                log.error("消息發送到交換機失敗: {}", cause);
            }
        });
        return rabbitTemplate;
    }

}
