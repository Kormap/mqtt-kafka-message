package org.com.message.mqtt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.com.message.mqtt.service.MqttService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mqtt")
@RequiredArgsConstructor
public class MQTTController {
    private final MqttService mqttService;

    @Operation(summary = "MQTT 발행(pub) API", description = "MQTT 발행(pub)을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "sensor data post : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    // MQTT 메시지 발행
    @PostMapping("/publish")
    public String publish(@RequestParam String topic, @RequestParam String message) {
        mqttService.publishMessage(topic, message);
        return "Message Published to topic: " + topic;
    }

    // MQTT 구독
    /**
     * MqttConfig 에 설정한 구독("topic/#) 외
     * 동적인 토픽을 구독하는 기능
     */
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String topic) {
        mqttService.subscribeToTopic(topic);
        return "Subscribed to topic: " + topic;
    }
}
