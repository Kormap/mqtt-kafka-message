package org.com.message.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka 토픽에 메시지를 발행(Pub)하는 서비스
 * 메시지 송신 기능
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private static final String TOPIC = "kafka-demo";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        log.info("send message to topic : {}, message : {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, message);
    }
}
