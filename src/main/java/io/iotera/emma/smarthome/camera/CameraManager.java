package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class CameraManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    //////////////
    // Schedule //
    //////////////

    private ConcurrentHashMap<Long, CameraSchedule> schedulers = new ConcurrentHashMap<Long, CameraSchedule>();

    private CameraSchedule getSchedule(long accountId) {
        if (schedulers.containsKey(accountId)) {
            return schedulers.get(accountId);
        }

        CameraSchedule schedule = applicationContext.getBean(CameraSchedule.class);
        schedule.initSchedule(accountId);
        schedulers.put(accountId, schedule);

        return schedule;
    }

    public int getActiveScheduleCount(long accountId) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.getActiveScheduleCount();
    }

    public boolean putSchedule(long accountId, String cameraId) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.putSchedule(cameraId);
    }

    public boolean removeSchedule(long accountId, String cameraId) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(cameraId);
    }

}
