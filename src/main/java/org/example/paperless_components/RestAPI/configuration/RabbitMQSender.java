package org.example.paperless_components.RestAPI.configuration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String message) {
        rabbitTemplate.convertAndSend("myQueue", message);
    }
}