package io.iotera.emma.smarthome.config.mqtt;

/*@Configuration*/
public class MqttRoutinePublisherConfig {

    /*
    private static final String CLIENT_ID = "mqtt-routine-publisher";

    @Autowired
    MqttPahoClientFactory mqttClientFactory;

    @Bean(name = "mqttRoutinePublisherChannel")
    public MessageChannel mqttRoutinePublisherChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttRoutinePublisherChannel")
    public MessageHandler mqttRoutinePublisherHandler() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                CLIENT_ID + "-" + new Date().getTime(),
                mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setAsyncEvents(true);
        messageHandler.setDefaultQos(2);
        return messageHandler;
    }

    @MessagingGateway(defaultRequestChannel = "mqttRoutinePublisherChannel")
    public interface MqttRoutinePublisherGateway {
        void send(Message<String> message);
    }
    */

}
