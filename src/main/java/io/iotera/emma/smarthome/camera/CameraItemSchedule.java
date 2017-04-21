package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.util.Tuple;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Component
@Scope("prototype")
public class CameraItemSchedule implements ApplicationContextAware {

    private final String CRON_SCHEDULE = "0 55 * * * ?";

    private ApplicationContext applicationContext;
    private long hubId;
    private String cameraId;

    private ThreadPoolTaskScheduler taskScheduler;
    private Future cameraInit;
    private Future cameraStartSchedule;
    private Future cameraStopSchedule1;
    private Future cameraStopSchedule2;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void initSchedule(long hubId, String cameraId) {
        this.hubId = hubId;
        this.cameraId = cameraId;
        this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("cameraThreadPoolTaskScheduler");
    }

    boolean updateCameraScheduleOnApplicationReady(List<Tuple.T2<Date, YoutubeItem>> stopSchedules) {
        removeSchedule();

        CameraStartTask taskSchedule = applicationContext.getBean(CameraStartTask.class);
        taskSchedule.initTask(hubId, cameraId, null);

        this.cameraStartSchedule = this.taskScheduler.schedule(taskSchedule,
                new CronTrigger(CRON_SCHEDULE));

        for (Tuple.T2<Date, YoutubeItem> stopSchedule : stopSchedules) {
            updateCameraStopSchedule(stopSchedule._1, stopSchedule._2);
        }

        return true;
    }

    boolean updateCameraStartSchedule(CameraStartTaskItem item) {
        removeSchedule();

        CameraStartTask taskInit = applicationContext.getBean(CameraStartTask.class);
        taskInit.initTask(hubId, cameraId, item);

        CameraStartTask taskSchedule = applicationContext.getBean(CameraStartTask.class);
        taskSchedule.initTask(hubId, cameraId, null);

        this.cameraInit = this.taskScheduler.submit(taskInit);
        this.cameraStartSchedule = this.taskScheduler.schedule(taskSchedule,
                new CronTrigger(CRON_SCHEDULE));

        return true;
    }

    boolean updateCameraStopSchedule(Date time, YoutubeItem item) {

        if (this.cameraStopSchedule1 == null || this.cameraStopSchedule1.isCancelled() || this.cameraStopSchedule1.isDone()) {
            CameraStopTask stopTask = applicationContext.getBean(CameraStopTask.class);
            stopTask.initTask(hubId, cameraId, item);
            this.cameraStopSchedule1 = this.taskScheduler.schedule(stopTask, time);
            return true;
        }

        if (this.cameraStopSchedule2 == null || this.cameraStopSchedule2.isCancelled() || this.cameraStopSchedule2.isDone()) {
            CameraStopTask stopTask = applicationContext.getBean(CameraStopTask.class);
            stopTask.initTask(hubId, cameraId, item);
            this.cameraStopSchedule2 = this.taskScheduler.schedule(stopTask, time);
            return true;
        }

        return false;
    }

    boolean removeCamera() {
        removeSchedule();

        if (this.taskScheduler != null) {
            this.taskScheduler.destroy();
            this.taskScheduler = null;
        }

        return true;
    }

    boolean removeSchedule() {
        if (cameraInit != null && !cameraInit.isCancelled() && !cameraInit.isDone()) {
            cameraInit.cancel(true);
        }

        if (cameraStartSchedule != null && !cameraStartSchedule.isCancelled() && !cameraStartSchedule.isDone()) {
            cameraStartSchedule.cancel(true);
        }

        if (cameraStopSchedule1 != null && !cameraStopSchedule1.isCancelled() && !cameraStopSchedule1.isDone()) {
            cameraStopSchedule1.cancel(true);
        }

        if (cameraStopSchedule2 != null && !cameraStopSchedule2.isCancelled() && !cameraStopSchedule2.isDone()) {
            cameraStopSchedule2.cancel(true);
        }

        return true;
    }

}
