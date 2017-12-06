package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
//import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepo;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.repository.ESHubCameraRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Scope("prototype")
public class CameraRemoveTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    ESDeviceRepo.ESDeviceJRepo deviceJRepo;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

    private long hubId;
    private String cameraId;
    private CameraRemoveTaskItem item;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long hubId, String cameraId, CameraRemoveTaskItem item) {
        this.hubId = hubId;
        this.cameraId = cameraId;
        this.item = item;
    }

    @Override
    public void run() {

        if (item == null) {
            // Camera remove task item null
            return;
        }

        Date now = item.getTime();
        String clientId = item.getClientId();
        String clientSecret = item.getClietSecret();
        String accessToken = item.getAccessToken();
        String refreshToken = item.getRefreshToken();

        // Obtain Camera
        ESDevice camera = deviceRepo.findByDeviceId(cameraId, hubId);
        if (camera == null) {
            return;
        }
        String infoString = camera.getInfo();
        List<ESCameraHistory> histories = cameraHistoryRepo.listHistoryByCameraId(cameraId, hubId);

        // Delete camera on database
        camera.setDeleted(true);
        camera.setDeletedTime(now);
        deviceJRepo.saveAndFlush(camera);

        // Delete history on database
        deviceRepo.deleteChild(now, cameraId, hubId);

        // Obtain old info
        ObjectNode info = Json.parseToObjectNode(infoString);
        if (!Json.isObjectNodeHavingAllKeys(info, "ybid", "ysid", "ysk", "yurl", "tm")) {
            return;
        }
        String ybid = info.get("ybid").asText("");

        // Delete live stream
        int responseCode = youtubeService.deleteEvent(ybid, accessToken);
        if (responseCode == 401) {
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
            youtubeService.deleteEvent(ybid, accessToken);
        }

        // Delete history stream
        for (ESCameraHistory history : histories) {
            ybid = history.getYoutubeBroadcastId();
            responseCode = youtubeService.deleteEvent(ybid, accessToken);
            if (responseCode == 401) {
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
                youtubeService.deleteEvent(ybid, accessToken);
            }
        }

        String tmString = info.get("tm").asText("");
        ObjectNode payload = Json.buildObjectNode();
        payload.put("tm", tmString);

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
//            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_STOP, message));
//            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_START, message2));
        }


    }
}
