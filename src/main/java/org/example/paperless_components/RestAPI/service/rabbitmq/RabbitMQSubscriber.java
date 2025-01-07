package org.example.paperless_components.RestAPI.service.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;

@Service
public class RabbitMQSubscriber {

    @RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}
