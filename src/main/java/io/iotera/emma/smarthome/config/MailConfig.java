package io.iotera.emma.smarthome.config;

import io.iotera.emma.smarthome.mail.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Autowired
    Environment env;

    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(env.getProperty("mail.host"));
        javaMailSender.setUsername(env.getProperty("mail.username"));
        javaMailSender.setPassword(env.getProperty("mail.password"));
        javaMailSender.setJavaMailProperties(mailProperties());

        return javaMailSender;
    }

    @Bean
    public MailHelper mailHelper() {
        return new MailHelper();
    }

    private Properties mailProperties() {
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.transport.protocol", env.getProperty("mail.transport.protocol"));
        mailProperties.setProperty("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
        mailProperties.setProperty("mail.smtp.socketFactory.port", env.getProperty("mail.smtp.socketFactory.port"));
        mailProperties.setProperty("mail.smtp.socketFactory.class", env.getProperty("mail.smtp.socketFactory.class"));

        return mailProperties;
    }

}
