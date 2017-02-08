package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Component
@Scope("prototype")
public class CameraItemSchedule implements ApplicationContextAware {

    private final String CRON_SCHEDULE = "0 55 * * * ?";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private String cameraId;
    private long accountId;

    private ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture cameraInitSchedule;
    private ScheduledFuture cameraStartSchedule;
    private ScheduledFuture cameraStopSchedule1;
    private ScheduledFuture cameraStopSchedule2;

    void initSchedule(String cameraId, long accountId) {
        this.cameraId = cameraId;
        this.accountId = accountId;

        this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("cameraThreadPoolTaskScheduler");

        CameraStartTask taskInit = applicationContext.getBean(CameraStartTask.class);
        taskInit.initTask(accountId, cameraId, false);

        CameraStartTask taskSchedule = applicationContext.getBean(CameraStartTask.class);
        taskSchedule.initTask(accountId, cameraId, true);

        this.cameraInitSchedule = this.taskScheduler.scheduleWithFixedDelay(taskInit, 1000);
        this.cameraStartSchedule = this.taskScheduler.schedule(taskSchedule,
                new CronTrigger(CRON_SCHEDULE));
    }

    void updateCameraStopSchedule(String broadcastId, Date time) {

        if (this.cameraStopSchedule1 == null || this.cameraStopSchedule1.isCancelled() || this.cameraStopSchedule1.isDone()) {
            CameraStopTask taskStop = applicationContext.getBean(CameraStopTask.class);
            taskStop.initTask(accountId, cameraId, broadcastId, true);
            this.cameraStopSchedule1 = this.taskScheduler.schedule(taskStop, time);
            return;
        }

        if (this.cameraStopSchedule2 == null || this.cameraStopSchedule2.isCancelled() || this.cameraStopSchedule2.isDone()) {
            CameraStopTask taskStop = applicationContext.getBean(CameraStopTask.class);
            taskStop.initTask(accountId, cameraId, broadcastId, true);
            this.cameraStopSchedule2 = this.taskScheduler.schedule(taskStop, time);
            return;
        }

    }

    boolean removeCamera() {
        if (cameraInitSchedule != null) {
            cameraInitSchedule.cancel(true);
        }

        if (cameraStartSchedule != null) {
            cameraStartSchedule.cancel(true);
        }

        if (cameraStopSchedule1 != null) {
            cameraStopSchedule1.cancel(true);
        }

        if (cameraStopSchedule2 != null) {
            cameraStopSchedule2.cancel(true);
        }

        if (this.taskScheduler != null) {
            this.taskScheduler.destroy();
            this.taskScheduler = null;
        }

        return true;
    }

}
