package org.example.paperless_components.RestAPI.service.rabbitmq;

import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;

@Slf4j
public class RabbitMQService {

    private RabbitMQConfig config = new RabbitMQConfig();
    RabbitTemplate rabbitTemplate = config.rabbitTemplate();
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
    }
}
