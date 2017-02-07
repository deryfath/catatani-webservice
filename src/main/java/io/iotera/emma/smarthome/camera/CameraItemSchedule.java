package io.iotera.emma.smarthome.camera;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Component
@Scope("prototype")
public class CameraItemSchedule implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    String cameraId;

    ScheduledFuture cameraStartSchedule1;
    ScheduledFuture cameraStopSchedule1;
    Date cameraTime1;

    ScheduledFuture cameraStartSchedule2;
    ScheduledFuture cameraStopSchedule2;
    Date cameraTime2;

    void initSchedule(String cameraId) {
        this.cameraId = cameraId;
        this.cameraTime1 = new Date();
        this.cameraTime2 = new Date();
    }

    boolean putSchedule(Date date, ScheduledFuture future) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();

        if (date.compareTo(cameraTime1) < 0 || date.compareTo(cameraTime2) < 0) {
            return false;
        }

        if (cameraTime1.compareTo(cameraTime2) < 0) {
            if (cameraStartSchedule1 != null) {
                cameraStartSchedule1.cancel(true);
            }

            if (cameraStopSchedule1 != null) {
                cameraStopSchedule1.cancel(true);
            }

            cameraTime1 = date;

        } else {

        }

        return true;
    }

    boolean removeCamera() {
        if (cameraStartSchedule1 != null) {
            cameraStartSchedule1.cancel(true);
        }

        if (cameraStopSchedule1 != null) {
            cameraStopSchedule1.cancel(true);
        }

        if (cameraStartSchedule2 != null) {
            cameraStartSchedule2.cancel(true);
        }

        if (cameraStopSchedule2 != null) {
            cameraStopSchedule2.cancel(true);
        }

        return true;
    }

}
