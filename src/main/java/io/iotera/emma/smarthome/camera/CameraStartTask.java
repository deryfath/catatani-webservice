package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setTask(String cameraId, long accountId) {
        this.cameraId = cameraId;
        this.accountId = accountId;

    }

    @Override
    public void run() {

        Tuple.T2<String, String> clientApi = applicationInfoRepository.getClientIdAndClientSecret();
        if (clientApi == null) {
            return;
        }

        String clientId = clientApi._1;
        String clientSecret = clientApi._2;

        Tuple.T2<String, String> token = accountCameraRepository.getAccessTokenAndRefreshToken(accountId);
        if (token == null) {
            //TODO Token not found
            return;
        }

        String accessToken = token._1;
        String refreshToken = token._2;





    }

}
