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
    private long hubId;
    private ConcurrentHashMap<String, CameraItemSchedule> cameraItemSchedules;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void initSchedule(long hubId) {
        this.hubId = hubId;
        this.cameraItemSchedules = new ConcurrentHashMap<String, CameraItemSchedule>();
    }

    boolean putSchedule(ESDevice device, String label, ObjectNode createObject) {

        if (!this.cameraItemSchedules.containsKey(device.getId())) {
            CameraItemSchedule schedule = applicationContext.getBean(CameraItemSchedule.class);
            schedule.initSchedule(device, hubId, label, createObject);
            this.cameraItemSchedules.put(device.getId(), schedule);
            return schedule.updateCameraStartSchedule();
        }

        return false;
    }

    boolean updateStopSchedule(ObjectNode stopParam, Date time) {

        if (this.cameraItemSchedules.containsKey(stopParam.get("device_id").textValue())) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(stopParam.get("device_id").textValue());
            return cameraItemSchedule.updateCameraStopSchedule(stopParam, time);
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
