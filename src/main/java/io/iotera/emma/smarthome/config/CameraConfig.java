package io.iotera.emma.smarthome.config;

import io.iotera.emma.smarthome.camera.CameraManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class CameraConfig {

    @Bean
    public CameraManager cameraManager() {
        return new CameraManager();
    }

    @Bean(name = "cameraThreadPoolTaskScheduler")
    @Scope("prototype")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(4);
        taskScheduler.setRemoveOnCancelPolicy(true);
        return taskScheduler;
    }


}
