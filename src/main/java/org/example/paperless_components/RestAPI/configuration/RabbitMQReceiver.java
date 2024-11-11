package org.example.paperless_components.RestAPI.configuration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQReceiver {

    @RabbitListener(queues = "myQueue")
    public void receive(String message) {
        System.out.println("Received Message: " + message);
    }
}