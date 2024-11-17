package org.example.paperless_components.RestAPI.service.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final RabbitMQPublisher publisher;

    @Autowired
    public MessageController(RabbitMQPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        publisher.sendMessage(message);
        return ResponseEntity.ok("Message sent: " + message);
    }
}

