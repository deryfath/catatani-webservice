package io.iotera.emma.smarthome.mqtt;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component("mqttMessageHandler")
public class MqttMessageHandler implements MessageHandler, ApplicationEventPublisherAware {

    @Autowired
    ESDeviceRepo deviceRepo;
    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void handleMessage(Message<?> amessage) throws MessagingException {

        final Message<?> message = amessage;

        MessageHeaders messageHeaders = message.getHeaders();
        String topic = (String) messageHeaders.get(MqttHeaders.TOPIC);
        String payloadString = (String) message.getPayload();

        String[] token = topic.split("/");
        if (token.length < 3) {
            return;
        }

        long accountId;
        try {
            accountId = Long.parseLong(token[1]);
        } catch (NumberFormatException e) {
            return;
        }

        String type = token[2];

        ObjectNode payload = Json.parseToObjectNode(payloadString);
        if (payload == null) {
            return;
        }

        if (type.equals(CommandPref.CONTROL)) {
            if (!payload.has("result_code") || !payload.has("command_id")
                    || !payload.has("device_id") || !payload.has("device_category")
                    || !payload.has("control")) {
                return;
            }

            int resultCode = payload.get("result_code").intValue();
            String commandId = payload.get("command_id").textValue().trim();
            String deviceId = payload.get("device_id").textValue().trim();
            int category = payload.get("device_category").intValue();
            String control = payload.get("control").textValue().trim();

            if (resultCode == 0) {
                //deviceRepo.updateStatus(deviceId, category, control, accountId);
            }

                    /*
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.set(env.getProperty("fcm.authorization.key"),
                            "key=" + env.getProperty("fcm.api.key"));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    */

            ObjectNode controlResult = Json.buildObjectNode();
            controlResult.put("result_code", resultCode);
            controlResult.put("command_id", commandId);
            controlResult.put("device_id", deviceId);
            controlResult.put("device_category", category);
            controlResult.put("control", control);

            ObjectNode dataResult = Json.buildObjectNode();
            dataResult.put("type", "control");
            dataResult.set("control", controlResult);

                    /*
                    ObjectNode httpBody = Json.buildObjectNode();
                    httpBody.put("to", "/topics/hub-" + accountId);
                    httpBody.set("data", dataResult);

                    HttpEntity<String> request = new HttpEntity<String>(httpBody.toString(), httpHeaders);

                    for (int i = 0; i < 10; ++i) {
                        try {
                            RestTemplate restTemplate = new RestTemplate();
                            ESHubAccountCameraController<String> response = restTemplate.postForEntity(
                                    env.getProperty("fcm.url"), request, String.class);

                            if (response.getStatusCode() == HttpStatus.OK) {
                                break;
                            }

                        } catch (HttpClientErrorException e) {
                            e.printStackTrace();
                        }
                    }*/

            Message<String> sendMessage = MessageBuilder
                    .withPayload(Json.toStringIgnoreNull(dataResult))
                    .setHeader(MqttHeaders.TOPIC, PublishUtility.topic("hub", accountId, CommandPref.CONTROL))
                    .build();

            if (applicationEventPublisher != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CONTROL, sendMessage));
            }

        }

    }
}
