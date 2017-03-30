package io.iotera.emma.smarthome.mqtt;

import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;

public class MqttPublishEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Message<String> message;
    private String type;

    public MqttPublishEvent(Object source, String type, Message<String> message) {
        super(source);
        this.message = message;
        this.type = type;
    }

    public Message<String> getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

}
