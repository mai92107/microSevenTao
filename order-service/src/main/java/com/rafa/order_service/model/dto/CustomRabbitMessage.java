package com.rafa.order_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomRabbitMessage {

    private String routingKey;
    private String exchange;
    private Object message;
}
