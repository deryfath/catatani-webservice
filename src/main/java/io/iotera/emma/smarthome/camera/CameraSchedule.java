package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.youtube.YoutubeItem;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

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

    boolean putScheduleOnApplicationReady(String cameraId,
                                          Date time1, YoutubeItem item1,
                                          Date time2, YoutubeItem item2) {

        CameraItemSchedule schedule;
        if (!this.cameraItemSchedules.containsKey(cameraId)) {
            schedule = applicationContext.getBean(CameraItemSchedule.class);
            schedule.initSchedule(hubId, cameraId);
            this.cameraItemSchedules.put(cameraId, schedule);
        } else {
            schedule = this.cameraItemSchedules.get(cameraId);
        }

        return schedule.updateCameraScheduleOnApplicationReady(time1, item1, time2, item2);
    }

    boolean putSchedule(String cameraId, CameraStartTaskItem item) {

        CameraItemSchedule schedule;
        if (!this.cameraItemSchedules.containsKey(cameraId)) {
            schedule = applicationContext.getBean(CameraItemSchedule.class);
            schedule.initSchedule(hubId, cameraId);
            this.cameraItemSchedules.put(cameraId, schedule);
        } else {
            schedule = this.cameraItemSchedules.get(cameraId);
        }

        return schedule.updateCameraStartSchedule(item);
    }

    boolean updateStopSchedule(String cameraId, Date time, YoutubeItem item) {

        if (this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(cameraId);
            return cameraItemSchedule.updateCameraStopSchedule(time, item);
        }

        return false;
    }

    boolean removeSchedule(String cameraId, CameraRemoveTaskItem item) {

        if (this.cameraItemSchedules.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItemSchedules.get(cameraId);
            cameraItemSchedule.removeCamera();
            this.cameraItemSchedules.remove(cameraId);

            // Camera Remove Task
            CameraRemoveTask removeTask = applicationContext.getBean(CameraRemoveTask.class);
            removeTask.initTask(hubId, cameraId, item);
            Executors.newSingleThreadExecutor().submit(removeTask);
            return true;
        }

        return false;
    }


}
