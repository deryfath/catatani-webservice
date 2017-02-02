package io.iotera.emma.smarthome.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.event.MqttMessageDeliveredEvent;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@IntegrationComponentScan
public class MqttOutboundConfig {

    @Autowired
    Environment env;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(env.getProperty("mqtt.url"));
        factory.setUserName(env.getProperty("mqtt.user"));
        factory.setPassword(env.getProperty("mqtt.password"));
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                env.getProperty("mqtt.command.publisher.client.id") + "-" + new Date().getTime(),
                mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setAsyncEvents(true);
        messageHandler.setDefaultTopic(env.getProperty("mqtt.topic.command"));
        messageHandler.setDefaultQos(2);
        return messageHandler;
    }

    @Bean(name = "mqttOutboundChannel")
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttOutboundGateway {
        void send(Message<String> message);
    }

    @Bean
    public Map<String, DeferredResult<ResponseEntity>> mqttDefferedResults() {
        Map<String, DeferredResult<ResponseEntity>> map = new ConcurrentHashMap<String, DeferredResult<ResponseEntity>>();
        return map;
    }

    @Component
    public class ESMqttMessageDeliveredEventListener implements ApplicationListener<MqttMessageDeliveredEvent> {

        @Autowired
        public ESMqttMessageDeliveredEventListener() {
        }


        @Override
        public void onApplicationEvent(MqttMessageDeliveredEvent event) {

        }

    }

}
