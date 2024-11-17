package org.example.paperless_components.RestAPI.service.rabbitmq;

import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQService {
    private RabbitMQConfig config;
    private RabbitTemplate rabbitTemplate;

    public RabbitMQService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendMessageToQueue(String message) {
        rabbitTemplate.convertAndSend("",
                "Echo_In",  // Queue-Name
                message);  // Nachricht

        log.info("Message sent to queue: {}", message);
    }

    /*
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        log.info("\nYou received: " + message);
    };
    public void sendMessageToQueue(String message) throws IOException {
        config.connectionFactory()
                .createConnection()
                .createChannel(false)
                .basicConsume(RabbitMQConfig.ECHO_OUT_QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        log.info("You entered: " + message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ECHO_IN_QUEUE_NAME, message);
    }*/
}
