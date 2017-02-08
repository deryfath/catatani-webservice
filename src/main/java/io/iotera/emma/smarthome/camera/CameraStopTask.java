package io.iotera.emma.smarthome.camera;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CameraStopTask implements Runnable, ApplicationEventPublisherAware {

    private long accountId;
    private String cameraId;
    private String broadcastId;
    private boolean fromSchedule;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long accountId, String cameraId, String broadcastId, boolean fromSchedule) {
        this.accountId = accountId;
        this.cameraId = cameraId;
        this.broadcastId = broadcastId;
        this.fromSchedule = fromSchedule;
    }

    @Override
    public void run() {

        System.out.println("MASUK STOP");
        System.out.println(accountId);
        System.out.println(cameraId);
        System.out.println(broadcastId);
        System.out.println(fromSchedule);

        if (!fromSchedule) {

        } else {

        }


        // YOUTUBE COMPLETE
        // MQTT

    }

}
