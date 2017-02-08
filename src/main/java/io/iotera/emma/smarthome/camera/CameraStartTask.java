package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.util.Tuple;
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

        Calendar calendar = Calendar.getInstance();
        Date time;
        Date stopTime;

        String broadcastId = "";

        if (fromSchedule) {
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();

            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 5);
            stopTime = calendar.getTime();

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

        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();

            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 5);
            stopTime = calendar.getTime();
        }

        // PROLOG + TRANSITION EVENT

        // Add stop schedule
        cameraManager.updateStopSchedule(accountId, cameraId, broadcastId, stopTime);
    }


}
