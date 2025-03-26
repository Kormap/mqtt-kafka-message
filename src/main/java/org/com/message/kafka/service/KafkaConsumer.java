package org.com.message.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Kafka 토픽(메시지)을 구독(Sub)하는 서비스
 * 메시지 수신 기능
 */
@Service
@Slf4j
public class KafkaConsumer {

    /**
     * 지정된 토픽에서 메시지 수신할 메소드
     * Kafka 에서 메시지를 읽고, 처리하는 작업 수행
     **/
    @KafkaListener(topics = "kafka-demo", groupId = "kafka-demo")
    public void consume(String message) throws IOException {
        log.info("Kafka consumer received message: {}", message);
    }
}
