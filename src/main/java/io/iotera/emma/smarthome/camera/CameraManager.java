package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.util.Tuple;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;
import java.util.List;
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

    public boolean putScheduleOnApplicationReady(long hubId, String cameraId, List<Tuple.T2<Date, YoutubeItem>> stopSchedules) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.putScheduleOnApplicationReady(cameraId, stopSchedules);
    }

    public boolean putSchedule(long hubId, String cameraId, CameraStartTaskItem item) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.putSchedule(cameraId, item);
    }

    public boolean updateStopSchedule(long hubId, String cameraId, Date time, YoutubeItem item) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.updateStopSchedule(cameraId, time, item);
    }

    public boolean removeSchedule(long hubId, String cameraId, CameraRemoveTaskItem item) {
        CameraSchedule schedule = getSchedule(hubId);
        return schedule.removeSchedule(cameraId, item);
    }


}
