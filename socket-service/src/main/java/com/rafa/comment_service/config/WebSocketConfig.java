package com.rafa.comment_service.config;

import com.rafa.comment_service.controller.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        // 註冊 WebSocket 端點
        exporter.setAnnotatedEndpointClasses(Channel.class);

        return exporter;
    }

}
