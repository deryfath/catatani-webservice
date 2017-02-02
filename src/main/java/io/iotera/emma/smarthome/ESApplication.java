package io.iotera.emma.smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@SpringBootApplication
public class ESApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ESApplication.class);
    }

    private static ConfigurableApplicationContext context = null;

    public static void main(String[] args) {
        context = start(args);
    }

    public static ConfigurableApplicationContext start(String[] args) {
        return SpringApplication.run(ESApplication.class, args);
    }

    public static void stop() {
        if (context != null) {
            context.close();
        }
    }

}
