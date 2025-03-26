package org.com.message.mqtt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class MqttWebSocketHandler extends TextWebSocketHandler {
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Map<String, Set<WebSocketSession>> topicSubscribers = new ConcurrentHashMap<>(); // 토픽을 구독하는 WebSocket 세션 관리 (Key: 토픽, Value: 구독한 세션 목록)
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper objectMapper;

    public MqttWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /* 웹소켓 연결 후 실행되는 메소드 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);  // 연결된 세션을 리스트에 추가
        log.info("WebSocket 연결 성공: {}", session.getId());
    }

    /* 웹소켓 연결 종료 후 실행되는 메소드 */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // 모든 구독 목록에서 세션 제거
        topicSubscribers.values().forEach(subscriber -> subscriber.remove(session));
        System.out.println("WebSocket 연결 종료: " + session.getId());
    }

    // WebSocket 클라이언트 메시지 요청 핸들링
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> receivedData = objectMapper.readValue(message.getPayload(), Map.class);
            // TODO : 클라이언트에서 action, topic 를 JSON 형태로 보내기(규약)
            // action : subscribe, publish | topic : topic 경로
            String action = receivedData.get("action").toString();
            String topic = receivedData.get("topic").toString();

            if ("subscribe".equals(action)) {
                subscribeToTopic(session, topic);
            } else if ("publish".equals(action)) {
                String payload = receivedData.get("payload").toString();
                log.info("클라이언트 : MQTT 메시지 발행(pub), 토픽: {}, 메시지 : {}", topic, payload);
            }
        } catch (Exception e) {
            log.error("클라이언트 메시지 처리 오류 : {}", e.getMessage());
        }
    }

    // WebSocket 클라이언트를 MQTT 토픽 구독자로 등록
    private void subscribeToTopic(WebSocketSession session, String topic) {
        topicSubscribers.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket 클라이언트: {}, 토픽: {}", session.getId(), topic);
    }

    // MQTT 메시지 수신(subscribe) 시 해당 토픽 구독한 WebSocket 클라이언트에게 전송
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(Message<?> message) {
        try {
            // MQTT 메시지 정보
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);    // topic
            String payload = message.getPayload().toString();   // 본문(payload)

            // 전송 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("topic", topic);
            response.put("payload", payload);
            response.put("timestamp", System.currentTimeMillis());

            String jsonResponse = objectMapper.writeValueAsString(response);
            // 해당 토픽 구독한 WebSocket 클라이언트에게 전송
            Set<WebSocketSession> subscribers = topicSubscribers.get(topic);
            if (subscribers != null) {
                for (WebSocketSession session : subscribers) {
                    // WebSocket 연결이 된 클라이언트에게 전송
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonResponse));
                    }
                }
                log.info("MQTT 메시지 : WebSocket 연결된 클라이언트에게 전송완료. 데이터: {}", jsonResponse);
            }

        } catch (Exception e) {
            log.info("WebSocket 메시지 전송 오류: {}", e.getMessage());
        }
    }
}
