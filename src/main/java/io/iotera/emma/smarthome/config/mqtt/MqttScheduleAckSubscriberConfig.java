//package io.iotera.emma.smarthome.config.mqtt;
//
//import io.iotera.emma.smarthome.mqtt.message.MqttScheduleAckMessageHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.channel.DirectChannel;
//import org.springframework.integration.core.MessageProducer;
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
//import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
//import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.MessageHandler;
//
//import java.util.Date;
//
//@Configuration
//public class MqttScheduleAckSubscriberConfig {
//
//    private static final String CLIENT_ID = "mqtt-scheduleack-subscriber";
//    private static final String TOPIC = "server/hub/+/schedule_ack/+";
//
//    @Autowired
//    @Qualifier("mqttClientFactory")
//    MqttPahoClientFactory clientFactory;
//
//    @Autowired
//    @Qualifier("mqttScheduleAckMessageHandler")
//    MqttScheduleAckMessageHandler messageHandler;
//
//    @Bean(name = "mqttScheduleAckSubscriberChannel")
//    public MessageChannel mqttScheduleAckSubscriberChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    public MessageProducer mqttScheduleAckSubscriberProducer() {
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter(
//                        CLIENT_ID + "-" + new Date().getTime(),
//                        clientFactory,
//                        TOPIC
//                );
//        adapter.setCompletionTimeout(5000);
//        adapter.setConverter(new DefaultPahoMessageConverter());
//        adapter.setQos(2);
//        adapter.setOutputChannel(mqttScheduleAckSubscriberChannel());
//        return adapter;
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttScheduleAckSubscriberChannel")
//    public MessageHandler mqttScheduleAckSubscriberHandler() {
//        return messageHandler;
//    }
//
//}
