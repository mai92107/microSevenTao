package com.rafa.comment_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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
    public Queue syncUpdateAdminHotelScoreQueue(){
        return new Queue("updateAdminHotelScoreQueue",true,false,false);
    }
    @Bean
    public Queue syncUpdateUserHotelScoreQueue(){
        return new Queue("updateUserHotelScoreQueue",true,false,false);
    }

    @Bean
    public DirectExchange commentExchange(){
        return new DirectExchange("commentExchange");
    }

    @Bean
    public Binding updateAdminHotelScoreBinding(){
        return BindingBuilder.bind(syncUpdateAdminHotelScoreQueue()).to(commentExchange()).with("updateAdminHotelScoreRoute");
    }
    @Bean
    public Binding updateUserHotelScoreBinding(){
        return BindingBuilder.bind(syncUpdateUserHotelScoreQueue()).to(commentExchange()).with("updateUserHotelScoreRoute");
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
