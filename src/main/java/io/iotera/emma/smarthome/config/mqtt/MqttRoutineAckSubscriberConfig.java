package io.iotera.emma.smarthome.config.mqtt;

/*@Configuration*/
public class MqttRoutineAckSubscriberConfig {

    /*
    private static final String CLIENT_ID = "mqtt-routine-ack-subscriber";
    private static final String TOPIC = "hub/+/routine_ack";

    @Autowired
    MqttPahoClientFactory mqttClientFactory;

    @Autowired
    @Qualifier("mqttMessageHandler")
    MqttMessageHandler messageHandler;

    @Bean(name = "mqttRoutineAckSubscriberChannel")
    public MessageChannel mqttRoutineAckSubscriberChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttRoutineAckSubscriberProducer() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        CLIENT_ID + "-" + new Date().getTime(),
                        mqttClientFactory,
                        TOPIC
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttRoutineAckSubscriberChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttRoutineAckSubscriberChannel")
    public MessageHandler mqttRoutineAckSubscriberHandler() {
        return messageHandler;
    }
    */

}
