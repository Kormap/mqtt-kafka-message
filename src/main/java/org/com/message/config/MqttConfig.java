package org.com.message.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
@IntegrationComponentScan   // 메시지 채널, 핸들러, 서비스 액티베이터 등의 빈 스캔, 등록
@Slf4j
public class MqttConfig {
    /* hiveMQ Cloud 브로커 설정*/
    @Value("${hivemq.cloud.url}")
    private String MQTT_BROKER_URL;

    @Value("${hivemq.client.username}")
    private String MQTT_USERNAME;

    @Value("${hivemq.client.password}")
    private String MQTT_USER_PASSWORD;

    private static final String CLIENT_ID = "test-client";

    /*
     *  MQTT 클라이언트 생성
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{MQTT_BROKER_URL});   // 브로커 URL 설정
        options.setUserName(MQTT_USERNAME);
        options.setPassword(MQTT_USER_PASSWORD.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);    // 자동 재연결 활성화
        factory.setConnectionOptions(options);

        return factory;
    }

    /**
      * 인바운드, 아웃바운드 채널 분리
      */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new QueueChannel();
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new QueueChannel();
    }

    /*
     *   인바운드 어댑터(Subscribe)
     *   MQTT 브로커로부터 메시지 수신
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttMessageSubscriber(MqttPahoClientFactory factory) {
        // 토픽 경로 설정 : topic/#
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID + "_inbound", factory, "topic/#");
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        log.info("Subscribing to topic : topic/#");
        return adapter;
    }

    /*
     *   아웃바운드 핸들러(Publisher)
     *   MQTT 브로커로 메시지 송신
     *   TODO 센서를 제어하는 용도로 사용 예정(추후 구현해보기)
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttMessagePublisher(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(CLIENT_ID, factory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(1);
        return messageHandler;
    }

    /**
     * MQTT 브로커에서 "수신" 된 메시지를 처리하는 핸들러
     * Spring Integration @ServiceActivator 어노테이션 적용
     * 'mqttInputChannel' 로 전달된 메시지를 처리
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                log.info("MQTT MessageHandler :: Start =======================");
                log.info("MQTT MessageHandler :: Header :: {}", message.getHeaders());
                log.info("MQTT MessageHandler :: Message :: {}", message.getPayload());
                log.info("MQTT MessageHandler :: Time :: {}", message.getHeaders().getTimestamp());
                log.info("MQTT MessageHandler :: End =======================");
            }
        };
    }
}
