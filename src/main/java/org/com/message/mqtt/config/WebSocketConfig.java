package org.com.message.mqtt.config;

import lombok.RequiredArgsConstructor;
import org.com.message.mqtt.handler.MqttWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final MqttWebSocketHandler mqttWebSocketHandler;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mqttWebSocketHandler, "/mqtt-websocket")
                .setAllowedOrigins("*");
    }
}
