package com.rafa.comment_service.config;

import com.rabbitmq.client.ShutdownSignalException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;


    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();

        try {
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);

            log.info("Attempting to create RabbitMQ connection with host: {}, port: {}", host, port);
            // SSL Configuration
            try {
                factory.getRabbitConnectionFactory().useSslProtocol(createSslContext());
            } catch (Exception e) {
                log.error("Error configuring SSL for RabbitMQ", e);
            }

            factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            factory.setPublisherReturns(true);

            ConnectionListener connectionListener = new ConnectionListener() {
                @Override
                public void onCreate(Connection connection) {
                    log.info("RabbitMQ Connection successfully established!");
                    log.info("Connection details - Local Port: {}", connection.getLocalPort());
                    log.info("Connected to: {}", connection.getDelegate().getAddress());
                }

                @Override
                public void onClose(Connection connection) {
                    log.warn("RabbitMQ Connection closed! Local Port: {}", connection.getLocalPort());
                }

                @Override
                public void onShutDown(ShutdownSignalException signal) {
                    log.error("RabbitMQ Connection shutdown! Reason: {}", signal.getMessage());
                }
            };

            factory.addConnectionListener(connectionListener);

        } catch (Exception e) {
            log.error("Error creating RabbitMQ connection factory", e);
            throw e;
        }
        return factory;
    }

    private SSLContext createSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }}, new SecureRandom());
        return sslContext;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setDefaultRequeueRejected(false);

        // 設置消費者並發數
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);

        // 設置預取數量
        factory.setPrefetchCount(1);

        // Error handler
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                new ConditionalRejectingErrorHandler.DefaultExceptionStrategy()));
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);


        return factory;
    }

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


    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
