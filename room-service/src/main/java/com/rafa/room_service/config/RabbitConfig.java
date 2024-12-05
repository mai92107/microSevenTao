package com.rafa.room_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitConfig {

//    @Bean
//    public Queue syncCreateRoomQueue() {
//        return new Queue("createRoomQueue", true, true, false);
//    }
//
//    @Bean
//    public Queue syncDeleteRoomQueue() {
//        return new Queue("deleteRoomQueue", true, true, false);
//    }
//
//    @Bean
//    public DirectExchange roomExchange() {
//        return new DirectExchange("roomExchange");
//    }
//
//    @Bean
//    public Binding createRoomBinding(Queue syncCreateRoomQueue, DirectExchange roomExchange) {
//        return BindingBuilder.bind(syncCreateRoomQueue).to(roomExchange).with("createRoomQueue");
//    }
//
//    @Bean
//    public Binding deleteRoomBinding(Queue syncDeleteRoomQueue, DirectExchange roomExchange) {
//        return BindingBuilder.bind(syncDeleteRoomQueue).to(roomExchange).with("deleteRoomRoute");
//    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
//        rabbitTemplate.setMessageConverter(converter());
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            if (ack) {
//                log.info("消息已成功發送到交換機");
//            } else {
//                log.error("消息發送到交換機失敗: {}", cause);
//            }
//        });
//        return rabbitTemplate;
//    }


    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
