package io.iotera.emma.smarthome.application;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
public class ESApplicationListener implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        System.out.println(new Date());
        System.out.println(applicationEvent.getClass().getCanonicalName());
    }

}
