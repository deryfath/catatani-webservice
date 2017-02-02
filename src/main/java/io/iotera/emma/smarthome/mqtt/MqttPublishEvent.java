package io.iotera.emma.smarthome.mqtt;

import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;

public class MqttPublishEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Message<String> message;

    public MqttPublishEvent(Object source, Message<String> message) {
        super(source);
        this.message = message;
    }

    public Message<String> getMessage() {
        return message;
    }

}
