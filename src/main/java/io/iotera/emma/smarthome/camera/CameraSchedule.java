package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESDevice;
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

    boolean putSchedule(ESDevice device, String label, ObjectNode createObject) {

        if (!this.cameraItemSchedules.containsKey(device.getId())) {
            CameraItemSchedule schedule = applicationContext.getBean(CameraItemSchedule.class);
            schedule.initSchedule(device, accountId, label, createObject);
            this.cameraItemSchedules.put(device.getId(), schedule);
            return schedule.updateCameraStartSchedule();
        }

        return false;
    }

    boolean updateStopSchedule(String cameraId, String broadcastId, Date time, String streamId) {

        if (this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(cameraId);
            return cameraItemSchedule.updateCameraStopSchedule(broadcastId, time, streamId);
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
