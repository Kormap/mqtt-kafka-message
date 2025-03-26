package org.com.message.kafka.controller;

import lombok.RequiredArgsConstructor;
import org.com.message.kafka.service.KafkaProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducer producer;

    @PostMapping("publish")
    public String sendMessage(@RequestParam("message") String message) {
        this.producer.sendMessage(message);
        return "Kafka Message sent successfully";
    }
}
