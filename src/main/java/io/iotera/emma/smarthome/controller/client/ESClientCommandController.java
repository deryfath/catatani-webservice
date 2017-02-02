package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.config.MqttOutboundConfig;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.utility.PublishUtility;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/command")
public class ESClientCommandController extends ESBaseController {

    @Autowired
    MqttOutboundConfig.MqttOutboundGateway gateway;

    @Autowired
    ESDeviceRepository deviceRepository;

    @RequestMapping(value = "/control/device", method = RequestMethod.POST)
    public ResponseEntity controlDevice(HttpEntity<String> entity) {

        // Request Header
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = accountAccess(accessToken, clientId);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String commandId = rget(body, "escommand");
        String deviceId = rget(body, "esdevice");
        String control = rget(body, "escontrol");

        ESDevice device = deviceRepository.findByDeviceId(deviceId, accountId);
        if (device == null) {
            return okJsonFailed(-1, "device_not_found");
        }

        // Command
        ObjectNode command = Json.buildObjectNode();
        command.put("command_id", commandId);
        command.put("device_id", deviceId);
        command.put("control", control);

        // Device
        String type = CommandPref.CONTROL;
        String topic = PublishUtility.topic(getProperty("mqtt.topic.command"), accountId, type);
        Message<String> message = MessageBuilder
                .withPayload(command.toString())
                .setHeader(MqttHeaders.TOPIC, topic)
                .build();

        gateway.send(message);

        return okJsonSuccess("");
    }

    @RequestMapping(value = "/camera/demand", method = RequestMethod.POST)
    public ResponseEntity demandCamera(HttpEntity<String> entity) {

        // Request Header
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = accountAccess(accessToken, clientId);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String commandId = rget(body, "escommand");
        String deviceId = rget(body, "esdevice");
        String control = rget(body, "escontrol");

        String type = CommandPref.CAMERA;
        String topic = PublishUtility.topic(getProperty("mqtt.command.publisher.default.topic"), accountId, type);
        Message<String> message = MessageBuilder
                .withPayload(body.toString())
                .setHeader(MqttHeaders.TOPIC, topic)
                .build();

        gateway.send(message);

        return okJsonSuccess("");
    }

}
