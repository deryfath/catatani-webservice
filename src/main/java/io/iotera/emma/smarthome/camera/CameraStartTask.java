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
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
@Scope("prototype")
public class CameraStartTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    @Autowired
    CameraManager cameraManager;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    PrologVideo prologVideo;

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

    @Autowired
    ESCameraHistoryRepo.ESCameraHistoryJRepo cameraHistoryJRepo;

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    ESDeviceRepo.ESDeviceJRepo deviceJRepo;

    private long hubId;
    private String cameraId;
    private CameraStartTaskItem item;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    public static Date toNearestWholeHour(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.MINUTE) <= 59)
            c.set(Calendar.HOUR, c.get(Calendar.HOUR));

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long hubId, String cameraId, CameraStartTaskItem item) {
        this.hubId = hubId;
        this.cameraId = cameraId;
        this.item = item;
    }

    @Override
    public void run() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time, stopTime;

        String title;
        String roomId;

        String accessToken, clientId, clientSecret, refreshToken;
        int maxQueue;
        YoutubeItem oldYoutubeItem, newYoutubeItem;

        if (item == null) {
            // From Schedule

            // Obtain Client Id and Client secret
            Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
            if (youtubeClientApi == null) {
                return;
            }

            clientId = youtubeClientApi._1;
            clientSecret = youtubeClientApi._2;

            // Obtain Access token and Refresh token
            ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
            if (hubCamera == null) {
                return;
            }

            accessToken = hubCamera.getAccessToken();
            refreshToken = hubCamera.getRefreshToken();
            maxQueue = hubCamera.getMaxHistory();

            // Obtain Camera
            ESDevice camera = deviceRepo.findByDeviceId(cameraId, hubId);
            if (camera == null) {
                return;
            }
            roomId = camera.getRoomId();
            String infoString = camera.getInfo();

            // Obtain old info
            ObjectNode info = Json.parseToObjectNode(infoString);
            if (!Json.isObjectNodeHavingAllKeys(info, "ybid", "ysid", "ysk", "yurl", "tm")) {
                return;
            }

            String ybid = info.get("ybid").asText("");
            String ysid = info.get("ysid").asText("");
            String ysk = info.get("ysk").asText("");
            String yurl = info.get("yurl").asText("");
            String tmString = info.get("tm").asText("");

            Date tm;
            try {
                tm = sdf.parse(tmString);
            } catch (ParseException e) {
                return;
            }

            // Update camera history shown
            cameraHistoryRepo.updateShownTrue(tm, cameraId, hubId);

            oldYoutubeItem = new YoutubeItem(ybid, ysid, ysk, yurl);
            oldYoutubeItem.setTime(tm);

            calendar.setTime(tm);
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();

            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 5);
            stopTime = calendar.getTime();

            title = camera.getLabel() + " " + sdf.format(time);

            Tuple.T2<Integer, YoutubeItem> prologResult = prologVideo.runVideoProlog(title, hubId);
            if (prologResult._1 != 0) {
                return;
            }
            newYoutubeItem = prologResult._2;
            newYoutubeItem.setTime(time);

            // Save new Camera info
            ObjectNode newInfo = Json.buildObjectNode();
            newInfo.setAll(oldYoutubeItem.getInfoOld());
            newInfo.setAll(newYoutubeItem.getInfo());
            if (!newInfo.isObject()) {
                return;
            }

            newInfo = Json.appendObjectNodeString(infoString, newInfo);
            System.out.println(newInfo);


            camera.setInfo(Json.toStringIgnoreNull(newInfo));

            deviceJRepo.saveAndFlush(camera);

        } else {
            // From Init
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();

            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 5);
            stopTime = calendar.getTime();

            title = item.getTitle();
            roomId = item.getRoomId();
            clientId = item.getClientId();
            clientSecret = item.getClietSecret();
            accessToken = item.getAccessToken();
            refreshToken = item.getRefreshToken();
            maxQueue = item.getMaxQueue();

            oldYoutubeItem = null;
            newYoutubeItem = item.getYoutubeItem();
        }

        ESCameraHistory cameraHistory =
                new ESCameraHistory(title, newYoutubeItem.getUrl(), newYoutubeItem.getBroadcastId(),
                        newYoutubeItem.getStreamId(), newYoutubeItem.getStreamKey(), time,
                        ESCameraHistory.parent(cameraId, roomId, hubId));

        cameraHistoryJRepo.saveAndFlush(cameraHistory);

        //DELETE OLD YOUTUBE LINK
        ResponseEntity cameraHistoryCount = cameraHistoryRepo.countRowHistoryCamera(cameraId, hubId);
        ObjectNode responseCamera = Json.parseToObjectNode(cameraHistoryCount.getBody().toString());
        System.out.println("responseCamera : " + responseCamera);
        if (responseCamera.size() != 0) {
            System.out.println("COUNT ROW HISTORY : " + responseCamera.get("count").toString() + " FIRST ID YOUTUBE : " + responseCamera.get("youtube_id").toString());
            if (Integer.parseInt(responseCamera.get("count").toString()) > maxQueue) {

                //DELETE FIRST ROW HISTORY CAMERA
                cameraHistoryRepo.deleteFirstRowByDeviceId(cameraId, hubId);

                //DELETE ON YOUTUBE API
                ResponseEntity responseEntityDelete = youtubeService.deleteEventById(accessToken, responseCamera.get("youtube_id").toString());
                ObjectNode responseBodyDelete = Json.parseToObjectNode(responseEntityDelete.getBody().toString());
                int statusCode = Integer.parseInt(responseBodyDelete.get("status_code").textValue());
                System.out.println(statusCode);

                if (responseBodyDelete.get("status_code") != null && statusCode == 401) {
                    System.out.println("UNAUTHORIZED");
                    //get access token by Refresh token
                    accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
                    responseEntityDelete = youtubeService.deleteEventById(accessToken, responseCamera.get("youtube_id").toString());
                    responseBodyDelete = Json.parseToObjectNode(responseEntityDelete.getBody().toString());
                    System.out.println("RESPONSE DELETE : " + responseBodyDelete);

                }

            }
        }

        ObjectNode payload = newYoutubeItem.getInfo();
        if (oldYoutubeItem != null) {
            //payload.put("yobid", oldYoutubeItem.getBroadcastId());
        }
        payload.put("url", "hub/" + hubId + "/camera_start/" + cameraId);

        Message<String> message = MessageBuilder
                .withPayload(payload.toString())
                .setHeader(MqttHeaders.TOPIC,
                        PublishUtility.topicHub(hubId, CommandPref.CAMERA_START, cameraId))
                .setHeader(MqttHeaders.RETAINED, true)
                .setHeader(MqttHeaders.QOS, 2)
                .build();

        if (applicationEventPublisher != null && message != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_START,
                    message));
        }

        cameraManager.updateStopSchedule(hubId, cameraId, stopTime, newYoutubeItem);
    }
}
