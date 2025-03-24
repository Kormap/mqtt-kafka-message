package org.com.message.dto;

import lombok.Data;

@Data
public class MqttDTO {
    private String sender;
    private String message;
}
