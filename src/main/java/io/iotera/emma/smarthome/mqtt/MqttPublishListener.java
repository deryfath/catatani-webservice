package io.iotera.emma.smarthome.mqtt;

import io.iotera.emma.smarthome.config.MqttOutboundConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MqttPublishListener implements ApplicationListener<MqttPublishEvent> {

    @Autowired
    MqttOutboundConfig.MqttOutboundGateway gateway;

    @Override
    public void onApplicationEvent(MqttPublishEvent event) {
        gateway.send(event.getMessage());
    }

}
