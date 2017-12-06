//package io.iotera.emma.smarthome.config;
//
////@Configuration
//public class MqttInboundConfig {
///*
//
//    @Autowired
//    Environment env;
//
//    @Autowired
//    @Qualifier("mqttMessageHandler")
//    MqttMessageHandler messageHandler;
//
//    @Bean(name = "mqttInboundChannel")
//    public MessageChannel mqttInboundChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    public MqttPahoClientFactory mqttClientFactory() {
//        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//        factory.setServerURIs(env.getProperty("mqtt.url.local"));
//        factory.setUserName(env.getProperty("mqtt.username"));
//        factory.setPassword(env.getProperty("mqtt.password"));
//        return factory;
//    }
//
//    @Bean
//    public MessageProducer mqttInbound() {
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter(
//                        env.getProperty("mqtt.url.local"),
//                        env.getProperty("mqtt.command.subscriber.client.id") + "-" + new Date().getTime(),
//                        mqttClientFactory(),
//                        env.getProperty("mqtt.command.result.wildcard.topic")
//                );
//        adapter.setCompletionTimeout(5000);
//        adapter.setConverter(new DefaultPahoMessageConverter());
//        adapter.setQos(2);
//        adapter.setOutputChannel(mqttInboundChannel());
//        return adapter;
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttInboundChannel")
//    public MessageHandler handler() {
//        return messageHandler;
//    }
//*/
//
//}
