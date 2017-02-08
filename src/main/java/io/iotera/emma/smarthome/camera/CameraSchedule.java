package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
public class CameraSchedule implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private long accountId;
    private ConcurrentHashMap<String, CameraItemSchedule> cameraItemSchedules;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void initSchedule(long accountId) {
        this.accountId = accountId;
        this.cameraItemSchedules = new ConcurrentHashMap<String, CameraItemSchedule>();
    }

    boolean putSchedule(String cameraId) {

        if (!this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule schedule = applicationContext.getBean(CameraItemSchedule.class);
            schedule.initSchedule(cameraId, accountId);
            return true;
        }

        return false;
    }

    boolean updateStopSchedule(String cameraId, String broadcastId, Date time) {

        if (!this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(cameraId);
            cameraItemSchedule.updateCameraStopSchedule(broadcastId, time);
            return true;
        }

        return false;
    }

    boolean removeSchedule(String cameraId) {

        if (this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(cameraId);
            cameraItemSchedule.removeCamera();
            this.cameraItemSchedules.remove(cameraId);
            return true;
        }

        return false;
    }

}
