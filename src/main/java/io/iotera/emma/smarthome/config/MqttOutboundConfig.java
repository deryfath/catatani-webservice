package io.iotera.emma.smarthome.config;

/*@Configuration
@IntegrationComponentScan*/
public class MqttOutboundConfig {
/*

    @Autowired
    Environment env;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(env.getProperty("mqtt.url.local"));
        factory.setUserName(env.getProperty("mqtt.username"));
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
*/

}
