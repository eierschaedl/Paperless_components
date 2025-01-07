package org.example.paperless_components.RestAPI.service.rabbitmq;

import org.example.paperless_components.RestAPI.api.DocumentAPI;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.example.paperless_components.RestAPI.configuration.RabbitMQConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RabbitMQSubscriber {
    private final DocumentAPI documentAPI;
    private final ObjectMapper objectMapper;

    public RabbitMQSubscriber(DocumentAPI documentAPI, ObjectMapper objectMapper) {
        this.documentAPI = documentAPI;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
    //@RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE)
    //@RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE_NAME)
    /*
    public void receiveMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String filePath = jsonNode.get("filePath").asText();
            String extractedText = jsonNode.get("extractedText").asText();

            documentAPI.updateText(filePath, extractedText);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
     */
}
