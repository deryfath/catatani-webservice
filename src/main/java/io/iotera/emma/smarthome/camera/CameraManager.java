package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CameraManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ConcurrentHashMap<Long, CameraSchedule> schedulers = new ConcurrentHashMap<Long, CameraSchedule>();

    //////////////
    // Schedule //
    //////////////

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private CameraSchedule getSchedule(long accountId) {
        if (schedulers.containsKey(accountId)) {
            return schedulers.get(accountId);
        }

        CameraSchedule schedule = applicationContext.getBean(CameraSchedule.class);
        schedule.initSchedule(accountId);
        schedulers.put(accountId, schedule);

        return schedule;
    }

    public boolean putSchedule(long accountId, String cameraId) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.putSchedule(cameraId);
    }

    public boolean updateStopSchedule(long accountId, String cameraId, String broadcastId, Date time) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.updateStopSchedule(cameraId, broadcastId, time);
    }

    public boolean removeSchedule(long accountId, String cameraId) {
        CameraSchedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(cameraId);
    }

}
