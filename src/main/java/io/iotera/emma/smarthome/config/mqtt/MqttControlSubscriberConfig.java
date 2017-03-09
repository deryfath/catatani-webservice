package io.iotera.emma.smarthome.config.mqtt;

/*@Configuration*/
public class MqttControlSubscriberConfig {

    /*
    private static final String CLIENT_ID = "mqtt-control-subscriber";
    private static final String TOPIC = "hub/+/control";

    @Autowired
    MqttPahoClientFactory mqttClientFactory;

    @Autowired
    @Qualifier("mqttMessageHandler")
    MqttMessageHandler messageHandler;

    @Bean(name = "mqttControlSubscriberChannel")
    public MessageChannel mqttControlSubscriberChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttControlSubscriberProducer() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        CLIENT_ID + "-" + new Date().getTime(),
                        mqttClientFactory,
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
    */

}
