package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
@Scope("prototype")
public class CameraStartTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    @Autowired
    ESApplicationInfoRepository applicationInfoRepository;

    @Autowired
    CameraManager cameraManager;

    private long accountId;
    private String cameraId;
    private boolean fromSchedule;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long accountId, String cameraId, boolean fromSchedule) {
        this.accountId = accountId;
        this.cameraId = cameraId;
        this.fromSchedule = fromSchedule;
    }

    @Override
    public void run() {

        Date time = new Date();
        Date stopTime = new Date();

        String broadcastId = "test";

        System.out.println("MASUK START");
        System.out.println(accountId);
        System.out.println(cameraId);
        System.out.println(fromSchedule);


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.MINUTE, 1);

        /*
        if (fromSchedule) {

            Tuple.T2<String, String> youtubeClientApi = applicationInfoRepository.getClientIdAndClientSecret();
            if (youtubeClientApi == null) {
                return;
            }

            String clientId = youtubeClientApi._1;
            String clientSecret = youtubeClientApi._2;

            Tuple.T2<String, String> token = accountCameraRepository.getAccessTokenAndRefreshToken(accountId);
            if (token == null) {
                //TODO Token not found
                return;
            }

            // CREATE YOUTUBE EVENT


        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();


        }
        */



        // CREATE YOUTUBE EVENT

        // PROLOG + TRANSITION EVENT

        // Add stop schedule
        cameraManager.updateStopSchedule(accountId, cameraId, broadcastId, calendar.getTime());
    }

}
