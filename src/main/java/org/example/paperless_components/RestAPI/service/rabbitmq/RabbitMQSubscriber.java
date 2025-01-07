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
        try {

            JsonNode jsonNode = objectMapper.readTree(message);

            String filePath = jsonNode.get("filename").asText();
            System.out.println(filePath);

            JsonNode extractedTextNode = jsonNode.get("extracted_text");
            if (extractedTextNode != null && extractedTextNode.isArray() && extractedTextNode.size() > 0) {
                String extractedText = extractedTextNode.get(0).asText();
                System.out.println("Extracted Text: " + extractedText);
            System.out.println(extractedText);

            documentAPI.updateText(filePath, extractedText);
            }
            else {
                System.err.println("Error: 'extracted_text' is missing or empty.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
