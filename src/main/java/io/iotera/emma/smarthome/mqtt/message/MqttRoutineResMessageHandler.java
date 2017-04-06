package io.iotera.emma.smarthome.mqtt.message;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.routine.RoutineResEvent;
import io.iotera.util.Json;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component("mqttRoutineResMessageHandler")
public class MqttRoutineResMessageHandler implements MessageHandler, ApplicationEventPublisherAware {

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void handleMessage(final Message<?> message) throws MessagingException {

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {

                MessageHeaders messageHeaders = message.getHeaders();
                String topic = (String) messageHeaders.get(MqttHeaders.TOPIC);

                String[] token = topic.split("/");
                if (token.length < 5) {
                    return;
                }

                long hubId = -1;
                try {
                    hubId = Long.parseLong(token[2]);
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                    return;
                }
                String routineId = token[4];

                String payloadString = (String) message.getPayload();
                ObjectNode payload = Json.parseToObjectNode(payloadString);
                if (payload == null) {
                    return;
                }

                if (applicationEventPublisher != null) {
                    applicationEventPublisher.publishEvent(new RoutineResEvent(
                            MqttRoutineResMessageHandler.this, hubId, routineId, payload));
                }

            }
        });

    }

}
