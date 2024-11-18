package com.rafa.order_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public Queue syncCreateOrderQueue(){
        return new Queue("createOrderQueue",true,false,false);
    }

    @Bean
    public Queue syncUserUpdateOrderQueue(){
        return new Queue("userUpdateOrderQueue",true,false,false);
    }

    @Bean
    public DirectExchange orderExchange(){
        return new DirectExchange("orderExchange");
    }

    @Bean
    public Binding createOrderBinding(){
        return BindingBuilder.bind(syncCreateOrderQueue()).to(orderExchange()).with("createOrderRoute");
    }

    @Bean
    public Binding userUpdateOrderBinding(){
        return BindingBuilder.bind(syncUserUpdateOrderQueue()).to(orderExchange()).with("userUpdateOrderRoute");
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
