package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESDevice;
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

    private CameraSchedule getSchedule(long hubId) {
        if (schedulers.containsKey(hubId)) {
            return schedulers.get(hubId);
        }

        CameraSchedule schedule = applicationContext.getBean(CameraSchedule.class);
        schedule.initSchedule(hubId);
        schedulers.put(hubId, schedule);

        return schedule;
    }

    public boolean putSchedule(long hubId, ESDevice device, String label, ObjectNode createObject) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.putSchedule(device, label, createObject);
    }

    public boolean updateStopSchedule(ObjectNode stopParam, Date time) {
        CameraSchedule schedule = getSchedule(stopParam.get("hub_id").asLong());
        return schedule.updateStopSchedule(stopParam, time);
    }

    public boolean removeSchedule(long hubId, String cameraId) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.removeSchedule(cameraId);
    }

}
