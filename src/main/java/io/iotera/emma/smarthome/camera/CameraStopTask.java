package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESHubCamera;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepo;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.repository.ESHubCameraRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Scope("prototype")
public class CameraStopTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    @Autowired
    ESCameraHistoryRepo.ESCameraHistoryJRepo cameraHistoryJRepo;

    private long hubId;
    private String cameraId;
    private YoutubeItem item;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long hubId, String cameraId, YoutubeItem item) {
        this.hubId = hubId;
        this.cameraId = cameraId;
        this.item = item;
    }

    @Override
    public void run() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (item == null) {
            // Youtube item null
            return;
        }

        // Obtain Client Id and Client secret
        Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
        if (youtubeClientApi == null) {
            return;
        }


        String clientId = youtubeClientApi._1;
        String clientSecret = youtubeClientApi._2;

        // Obtain Access token and Refresh token
        ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
        if (hubCamera == null) {
            return;
        }

        String accessToken = hubCamera.getAccessToken();
        String refreshToken = hubCamera.getRefreshToken();

        String ybid = item.getBroadcastId();
        String ysid = item.getStreamId();
        String ysk = item.getStreamKey();
        String yurl = item.getUrl();
        Date tm = item.getTime();

        // Obtain Camera
        ESDevice camera = deviceRepo.findByDeviceId(cameraId, hubId);
        if (camera == null) {
            return;
        }
        String title = camera.getLabel();
        ESCameraHistory cameraHistory =
                new ESCameraHistory(title+" "+sdf.format(tm), yurl, ybid, ysid, ysk, tm,
                        ESCameraHistory.parent(cameraId, camera.getRoomId(), hubId));
        cameraHistoryJRepo.saveAndFlush(cameraHistory);

        Tuple.T2<Integer, ObjectNode> response = youtubeService.transitionEventComplete(accessToken,
                ybid, null, "complete");
        if (response._1 == 401) {
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            System.out.println("CLIENT ID STOP : " + clientId);
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
            System.out.println("stop access token : " + accessToken);
            response = youtubeService.transitionEventComplete(accessToken, ybid, null, "complete");
            System.out.println(response);
        }

        ObjectNode payload = Json.buildObjectNode();
        payload.put("tm", sdf.format(tm));

        Message<String> message = MessageBuilder
                .withPayload(payload.toString())
                .setHeader(MqttHeaders.TOPIC,
                        PublishUtility.topicHub(hubId, CommandPref.CAMERA_STOP, cameraId))
                .setHeader(MqttHeaders.QOS, 2)
                .build();

        Message<String> message2 = MessageBuilder
                .withPayload("")
                .setHeader(MqttHeaders.TOPIC,
                        PublishUtility.topicHub(hubId, CommandPref.CAMERA_START, cameraId))
                .setHeader(MqttHeaders.RETAINED, true)
                .setHeader(MqttHeaders.QOS, 2)
                .build();

        if (applicationEventPublisher != null && message != null && message2 != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_STOP, message));
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_START, message2));
        }

    }

}
