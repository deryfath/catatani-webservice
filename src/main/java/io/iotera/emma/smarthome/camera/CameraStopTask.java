package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CameraStopTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    private long accountId;
    private String cameraId;
    private String broadcastId, streamID,clientId,clientSecret,accessToken,state, refreshToken;
    private boolean fromSchedule;
    private Message<String> message;
    private ObjectNode objectKey;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long accountId, String cameraId, String broadcastId, boolean fromSchedule, String streamID) {
        this.accountId = accountId;
        this.cameraId = cameraId;
        this.broadcastId = broadcastId;
        this.fromSchedule = fromSchedule;
        this.streamID = streamID;
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

        ResponseEntity responseYoutubeKey = accountCameraRepository.YoutubeKey(accountId);
        objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
        System.out.println("OBJECT KEY : " + objectKey);

        accessToken = objectKey.get("access_token").textValue();
        clientId = objectKey.get("client_id").textValue();
        clientSecret = objectKey.get("client_secret").textValue();
        refreshToken = objectKey.get("refresh_token").textValue();

        state = "complete";
        Tuple.T2<Integer, ObjectNode> responseTransitionStop = youtubeService.transitionEventComplete(accessToken, broadcastId, streamID, state);

        if (responseTransitionStop._1 == 401) {
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            System.out.println("CLIENT ID STOP : " + clientId);
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, accountId);
            System.out.println("stop access token : " + accessToken);
            responseTransitionStop = youtubeService.transitionEventComplete(accessToken, broadcastId, streamID, state);

        }
        System.out.println(responseTransitionStop);

        //MQTT MESSAGE
        this.message = MessageBuilder
                .withPayload(responseTransitionStop._2.toString())
                .setHeader(MqttHeaders.TOPIC,
                        "stream/transition/stop")
                .build();

        if (applicationEventPublisher != null && message != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
        } else {
            System.out.println("MQTT NULL");
        }

    }

}
