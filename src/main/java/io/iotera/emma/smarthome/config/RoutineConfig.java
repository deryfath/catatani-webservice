package io.iotera.emma.smarthome.config;

import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.routine.RoutineManagerYoutube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class RoutineConfig {

    @Bean
    public RoutineManager routineManager() {
        return new RoutineManager();
    }

    @Bean
    public RoutineManagerYoutube routineManagerYoutube() {
        return new RoutineManagerYoutube();
    }

    @Bean(name = "routineThreadPoolTaskScheduler")
    @Scope("prototype")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setRemoveOnCancelPolicy(true);
        return taskScheduler;
    }

}
