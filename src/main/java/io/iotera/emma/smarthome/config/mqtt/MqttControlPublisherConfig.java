package io.iotera.emma.smarthome.config.mqtt;

/*@Configuration*/
public class MqttControlPublisherConfig {

    /*
    private static final String CLIENT_ID = "mqtt-control-publisher";

    @Autowired
    MqttPahoClientFactory mqttClientFactory;

    @Bean(name = "mqttControlPublisherChannel")
    public MessageChannel mqttControlPublisherChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttControlPublisherChannel")
    public MessageHandler mqttControlPublisherHandler() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                CLIENT_ID + "-" + new Date().getTime(),
                mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setAsyncEvents(true);
        messageHandler.setDefaultQos(2);
        return messageHandler;
    }

    @MessagingGateway(defaultRequestChannel = "mqttControlPublisherChannel")
    public interface MqttControlPublisherGateway {
        void send(Message<String> message);
    }
    */

}
