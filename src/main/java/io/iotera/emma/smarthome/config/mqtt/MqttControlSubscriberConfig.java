package io.iotera.emma.smarthome.config.mqtt;

import io.iotera.emma.smarthome.mqtt.message.MqttControlMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Date;

@Configuration
public class MqttControlSubscriberConfig {

    private static final String CLIENT_ID = "mqtt-control-subscriber";
    private static final String TOPIC = "client/hub/+/control/+";

    @Autowired
    @Qualifier("mqttClientFactory")
    MqttPahoClientFactory clientFactory;

    @Autowired
    @Qualifier("mqttControlMessageHandler")
    MqttControlMessageHandler messageHandler;

    @Bean(name = "mqttControlSubscriberChannel")
    public MessageChannel mqttControlSubscriberChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttControlSubscriberProducer() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        CLIENT_ID + "-" + new Date().getTime(),
                        clientFactory,
                        TOPIC
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttControlSubscriberChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttControlSubscriberChannel")
    public MessageHandler mqttControlSubscriberHandler() {
        return messageHandler;
    }

}
