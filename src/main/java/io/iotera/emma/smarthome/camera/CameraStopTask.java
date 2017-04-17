package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESHubCameraRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
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
    ESHubCameraRepo hubCameraRepo;

    private long hubId;
    private String cameraId;
    private String broadcastId, streamID, clientId, clientSecret, accessToken, state, refreshToken;
    private boolean fromSchedule;
    private Message<String> message, message2;
    private ObjectNode objectKey;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long hubId, String cameraId, String broadcastId, boolean fromSchedule, String streamID) {
        this.hubId = hubId;
        this.cameraId = cameraId;
        this.broadcastId = broadcastId;
        this.fromSchedule = fromSchedule;
        this.streamID = streamID;
    }

    @Override
    public void run() {

        System.out.println("MASUK STOP");
        System.out.println(hubId);
        System.out.println(cameraId);
        System.out.println(broadcastId);
        System.out.println(fromSchedule);

        if (!fromSchedule) {

        } else {

        }

        // YOUTUBE COMPLETE
        // MQTT

        ResponseEntity responseYoutubeKey = hubCameraRepo.YoutubeKey(hubId);
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
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
            System.out.println("stop access token : " + accessToken);
            responseTransitionStop = youtubeService.transitionEventComplete(accessToken, broadcastId, streamID, state);

        }
        System.out.println(responseTransitionStop);

        //MQTT MESSAGE
        this.message = MessageBuilder
                .withPayload(responseTransitionStop._2.toString())
                .setHeader(MqttHeaders.TOPIC,
                        PublishUtility.topicHub(hubId, CommandPref.CAMERA_STOP, cameraId))
                .build();

        if (applicationEventPublisher != null && message != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_STOP, this.message));
        } else {
            System.out.println("MQTT NULL");
        }

        //MQTT SEND MESSAGE NULL CAMERA START
        this.message2 = MessageBuilder
                .withPayload("")
                .setHeader(MqttHeaders.TOPIC,
                        PublishUtility.topicHub(hubId, CommandPref.CAMERA_START, cameraId))
                .build();

        if (applicationEventPublisher != null && message2 != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_START, this.message2));
        } else {
            System.out.println("MQTT NULL");
        }

    }

}
