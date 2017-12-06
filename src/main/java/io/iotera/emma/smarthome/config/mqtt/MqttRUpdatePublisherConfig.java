//package io.iotera.emma.smarthome.config.mqtt;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.annotation.MessagingGateway;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.channel.DirectChannel;
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
//import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.MessageHandler;
//
//import java.util.Date;
//
//@Configuration
//public class MqttRUpdatePublisherConfig {
//
//    private static final String CLIENT_ID = "mqtt-rupdate-publisher";
//
//    @Autowired
//    @Qualifier("mqttClientFactory")
//    MqttPahoClientFactory clientFactory;
//
//    @Bean(name = "mqttRUpdatePublisherChannel")
//    public MessageChannel mqttRUpdatePublisherChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttRUpdatePublisherChannel")
//    public MessageHandler mqttRUpdatePublisherHandler() {
//        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
//                CLIENT_ID + "-" + new Date().getTime(),
//                clientFactory);
//        messageHandler.setAsync(true);
//        messageHandler.setAsyncEvents(true);
//        messageHandler.setDefaultQos(2);
//        return messageHandler;
//    }
//
//    @MessagingGateway(defaultRequestChannel = "mqttRUpdatePublisherChannel")
//    public interface MqttRUpdatePublisherGateway {
//        void send(Message<String> message);
//    }
//
//}
