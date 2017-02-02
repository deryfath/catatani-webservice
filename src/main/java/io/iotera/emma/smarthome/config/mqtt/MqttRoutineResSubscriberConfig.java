package io.iotera.emma.smarthome.config.mqtt;

import io.iotera.emma.smarthome.mqtt.MqttMessageHandler;
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

/*@Configuration*/
public class MqttRoutineResSubscriberConfig {

    /*
    private static final String CLIENT_ID = "mqtt-routine-res-subscriber";
    private static final String TOPIC = "hub/+/routine_res";

    @Autowired
    MqttPahoClientFactory mqttClientFactory;

    @Autowired
    @Qualifier("mqttMessageHandler")
    MqttMessageHandler messageHandler;

    @Bean(name = "mqttRoutineResSubscriberChannel")
    public MessageChannel mqttRoutineResSubscriberChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttRoutineResSubscriberProducer() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        CLIENT_ID + "-" + new Date().getTime(),
                        mqttClientFactory,
                        TOPIC
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttRoutineResSubscriberChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttRoutineResSubscriberChannel")
    public MessageHandler mqttRoutineResSubscriberHandler() {
        return messageHandler;
    }
    */

}
