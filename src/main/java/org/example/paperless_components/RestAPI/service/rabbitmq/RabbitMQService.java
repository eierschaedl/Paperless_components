package org.example.paperless_components.RestAPI.service.rabbitmq;

import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQService {
    private final ObjectMapper objectMapper;
    private RabbitTemplate rabbitTemplate;
    public RabbitMQService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    public void sendMessageToQueue(Object message) {
        message = message.toString();
        rabbitTemplate.convertAndSend("", "OCR_QUEUE", message);
        log.info("Message sent to queue: {}", message);
    }
}
