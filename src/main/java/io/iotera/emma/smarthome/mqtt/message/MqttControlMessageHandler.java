package io.iotera.emma.smarthome.mqtt.message;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.result.Result;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component("mqttControlMessageHandler")
public class MqttControlMessageHandler implements MessageHandler {

    @Autowired
    ESDeviceRepo deviceRepo;

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

                long accountId;
                try {
                    accountId = Long.parseLong(token[2]);
                } catch (NumberFormatException e) {
                    return;
                }

                String deviceId = token[4];
                String payloadString = (String) message.getPayload();
                ObjectNode payload = Json.parseToObjectNode(payloadString);
                if (payload == null) {
                    return;
                }

                if (!payload.has("cmid") || !payload.has("rc")
                        || !payload.has("dc") || !payload.has("co")
                        || !payload.has("cc")
                        ) {
                    return;
                }

                int resultCode = payload.get("rc").asInt(Result.UNKNOWN_ERROR);
                String control = payload.get("co").asText("");

                if (resultCode == Result.SUCCESS) {
                    deviceRepo.updateStatus(deviceId, control, accountId);
                }

            }
        });

    }


}
