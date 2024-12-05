package com.example.hotel_admin_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public Queue syncCreateHotelQueue() {
        return new Queue("createHotelQueue", true, false, false);
    }

    @Bean
    public Queue syncDeleteHotelQueue() {
        return new Queue("deleteHotelQueue", true, false, false);
    }

    @Bean
    public Queue syncUpdateHotelQueue() {
        return new Queue("updateHotelQueue", true, false, false);
    }

    @Bean
    public DirectExchange hotelExchange() {
        return new DirectExchange("hotelExchange");
    }

    @Bean
    public Binding createRoomBinding() {
        return BindingBuilder.bind(syncCreateHotelQueue()).to(hotelExchange()).with("createHotelRoute");
    }

    @Bean
    public Binding deleteRoomBinding() {
        return BindingBuilder.bind(syncDeleteHotelQueue()).to(hotelExchange()).with("deleteHotelRoute");
    }

    @Bean
    public Binding updateHotelBinding() {
        return BindingBuilder.bind(syncUpdateHotelQueue()).to(hotelExchange()).with("updateHotelRoute");
    }

    @Bean
    public MessageConverter converter() {
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

    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
