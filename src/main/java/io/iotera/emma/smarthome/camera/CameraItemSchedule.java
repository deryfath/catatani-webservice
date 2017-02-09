package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESDevice;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Future;

@Component
@Scope("prototype")
public class CameraItemSchedule implements ApplicationContextAware {

    private final String CRON_SCHEDULE = "0 55 * * * ?";

    private ApplicationContext applicationContext;
    private ESDevice device;
    private long accountId;
    private ObjectNode createObject;
    private String label;

    private ThreadPoolTaskScheduler taskScheduler;
    private Future cameraInit;
    private Future cameraStartSchedule;
    private Future cameraStopSchedule1;
    private Future cameraStopSchedule2;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void initSchedule(ESDevice device, long accountId, String label,ObjectNode createObject) {
        this.device = device;
        this.accountId = accountId;
        this.label = label;
        this.createObject = createObject;
        this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("cameraThreadPoolTaskScheduler");
    }

    boolean updateCameraStartSchedule() {
        removeSchedule();

        CameraStartTask taskInit = applicationContext.getBean(CameraStartTask.class);
        taskInit.initTask(accountId, this.device, false, label,createObject);

        CameraStartTask taskSchedule = applicationContext.getBean(CameraStartTask.class);
        taskSchedule.initTask(accountId, this.device, true, label,createObject);

        this.cameraInit = this.taskScheduler.submit(taskInit);
        this.cameraStartSchedule = this.taskScheduler.schedule(taskSchedule,
                new CronTrigger(CRON_SCHEDULE));

        return true;
    }

    boolean updateCameraStopSchedule(String broadcastId, Date time, String streamId) {

        if (this.cameraStopSchedule1 == null || this.cameraStopSchedule1.isCancelled() || this.cameraStopSchedule1.isDone()) {
            CameraStopTask taskStop = applicationContext.getBean(CameraStopTask.class);
            taskStop.initTask(accountId, this.device.getId(), broadcastId, true, streamId);
            this.cameraStopSchedule1 = this.taskScheduler.schedule(taskStop, time);
            return true;
        }

        if (this.cameraStopSchedule2 == null || this.cameraStopSchedule2.isCancelled() || this.cameraStopSchedule2.isDone()) {
            CameraStopTask taskStop = applicationContext.getBean(CameraStopTask.class);
            taskStop.initTask(accountId, this.device.getId(), broadcastId, true, streamId);
            this.cameraStopSchedule2 = this.taskScheduler.schedule(taskStop, time);
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
