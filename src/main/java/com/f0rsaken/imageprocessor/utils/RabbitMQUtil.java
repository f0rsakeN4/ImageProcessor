package com.f0rsaken.imageprocessor.utils;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendToQueue(String filePath) {
        rabbitTemplate.convertAndSend("imageProcessingQueue", filePath);
    }

}
