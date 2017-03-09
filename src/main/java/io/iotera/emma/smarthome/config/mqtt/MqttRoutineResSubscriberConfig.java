package io.iotera.emma.smarthome.config.mqtt;

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
