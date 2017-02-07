package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
public class CameraSchedule implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private long accountId;

    private ThreadPoolTaskScheduler taskScheduler;
    private ConcurrentHashMap<String, CameraItemSchedule> cameraItems;

    void initSchedule (long accountId) {
        this.accountId = accountId;
        this.cameraItems = new ConcurrentHashMap<String, CameraItemSchedule>();
    }

    boolean putSchedule(String cameraId) {
        if (this.taskScheduler == null && this.cameraItems.isEmpty()) {
            this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("cameraThreadPoolTaskScheduler");
        }

        if (!this.cameraItems.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = applicationContext.getBean(CameraItemSchedule.class);
            cameraItemSchedule.initSchedule(cameraId);
            //cameraItemSchedule.putSchedule();

            return true;
        }

        return false;
    }

    boolean removeSchedule(String cameraId) {
        if (this.cameraItems.containsKey(cameraId)) {
            CameraItemSchedule cameraItemSchedule = this.cameraItems.get(cameraId);
            cameraItemSchedule.removeCamera();
            this.cameraItems.remove(cameraId);

            if (this.cameraItems.isEmpty()) {
                this.taskScheduler.destroy();
                this.taskScheduler = null;
            }

            return true;
        }

        return false;
    }

    int getActiveScheduleCount() {
        return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
    }

}
