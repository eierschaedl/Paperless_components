package org.example.paperless_components.RestAPI.service.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;

@Component
@Slf4j
public class RabbitMQService {
    private final RabbitTemplate rabbit;

    @Autowired
    public RabbitMQService(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = RabbitMQConfig.ECHO_IN_QUEUE_NAME)
    public void processMessage(String message, @Header(RabbitMQConfig.ECHO_MESSAGE_COUNT_PROPERTY_NAME) int messageCount) {
        log.info("Recieved Message #" + messageCount+ ": " + message);

        // Delay
        try {
            Thread.sleep(message.length() * 1000L);
        } catch (InterruptedException e) {
            System.out.println("Delete2ndChar service interrupted");
        }

        rabbit.convertAndSend(RabbitMQConfig.ECHO_OUT_QUEUE_NAME, "Echo " + message);
        log.info("Sent Message: Echo " + message);
    }
}
