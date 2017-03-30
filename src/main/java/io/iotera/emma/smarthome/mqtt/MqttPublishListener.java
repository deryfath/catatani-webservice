package io.iotera.emma.smarthome.mqtt;

import io.iotera.emma.smarthome.config.mqtt.*;
import io.iotera.emma.smarthome.preference.CommandPref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MqttPublishListener implements ApplicationListener<MqttPublishEvent> {

    @Autowired
    MqttControlPublisherConfig.MqttControlPublisherGateway mqttControlPublisherGateway;

    @Autowired
    MqttRUpdatePublisherConfig.MqttRUpdatePublisherGateway mqttRUpdatePublisherGateway;

    @Autowired
    MqttSchedulePublisherConfig.MqttSchedulePublisherGateway mqttSchedulePublisherGateway;

    @Autowired
    MqttCameraStartPublisherConfig.MqttCameraStartPublisherGateway mqttCameraStartPublisherGateway;

    @Autowired
    MqttCameraStopPublisherConfig.MqttCameraStopPublisherGateway mqttCameraStopPublisherGateway;

    @Autowired
    MqttForceHomekickPublisherConfig.MqttForceHomekickPublisherGateway mqttForceHomekickPublisherGateway;

    @Autowired
    MqttForceLogoutPublisherConfig.MqttForceLogoutPublisherGateway mqttForceLogoutPublisherGateway;

    @Override
    public void onApplicationEvent(MqttPublishEvent event) {

        String type = event.getType();
        if (type.equals(CommandPref.CONTROL)) {
            mqttControlPublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.RUPDATE)) {
            mqttRUpdatePublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.SCHEDULE)) {
            mqttSchedulePublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.CAMERA_START)) {
            mqttCameraStartPublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.CAMERA_STOP)) {
            mqttCameraStopPublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.FORCE_HOMEKICK)) {
            mqttForceHomekickPublisherGateway.send(event.getMessage());
        } else if (type.equals(CommandPref.FORCE_LOGOUT)) {
            mqttForceLogoutPublisherGateway.send(event.getMessage());
        }

    }

}
