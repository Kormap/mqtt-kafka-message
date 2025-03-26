package org.com.message.mqtt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MqttService {
    private final MessageChannel mqttOutboundChannel;
    private final ApplicationContext applicationContext;


    public MqttService(MessageChannel mqttOutboundChannel, ApplicationContext applicationContext) {
        this.mqttOutboundChannel = mqttOutboundChannel;
        this.applicationContext = applicationContext;
    }

    // MQTT 메시지 발행 (아웃바운드)
    public void publishMessage(String topic, String message) {
        Message<String> msg = MessageBuilder.withPayload(message)
                .setHeader(MqttHeaders.TOPIC, topic)
                .build();
        mqttOutboundChannel.send(msg);
    }

    // MQTT 토픽 구독
    public void subscribeToTopic(String topic) {
        try {
            MqttPahoMessageDrivenChannelAdapter adapter =
                    applicationContext.getBean(MqttPahoMessageDrivenChannelAdapter.class);

            // 새로운 토픽 추가
            adapter.addTopic(topic, 1); // QoS 레벨(0, 1, 2)
            log.info("토픽 구독 성공: {}", topic);
        } catch (Exception e) {
            log.error("토픽 구독 실패: {}", topic, e);
            throw new RuntimeException("토픽 구독 중 오류 발생: " + e.getMessage(), e);
        }
    }

}

