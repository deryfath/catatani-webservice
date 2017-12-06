//package io.iotera.emma.smarthome.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import org.springframework.integration.annotation.IntegrationComponentScan;
//import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
//
//@Configuration
//@IntegrationComponentScan
//public class MqttConfig {
//
//    @Autowired
//    Environment env;
//
//    @Bean(name = "mqttClientFactory")
//    public MqttPahoClientFactory mqttClientFactory() {
//        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//        factory.setServerURIs(env.getProperty("mqtt.url.local"));
//        factory.setUserName(env.getProperty("mqtt.username"));
//        factory.setPassword(env.getProperty("mqtt.password"));
//        return factory;
//    }
//
//}
