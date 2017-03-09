package io.iotera.emma.smarthome.camera;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.camera.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepository;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.youtube.PrologVideo;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
@Scope("prototype")
public class CameraStartTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    @Autowired
    ESApplicationInfoRepository applicationInfoRepository;

    @Autowired
    CameraManager cameraManager;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    PrologVideo prologVideo;

    @Autowired
    ESCameraHistoryRepository historyCameraRepository;

    @Autowired
    ESCameraHistoryRepository.ESCameraHistoryJpaRepository deviceCameraHistoryJpaRepository;

    @Autowired
    ESDeviceRepository deviceRepository;

    private long accountId;
    private ESDevice device;
    private boolean fromSchedule;
    private ObjectNode objectKey;
    private String title, accessToken, clientId, clientSecret, refreshToken, oldBroadcastID;
    private String stateTask, mqttTime, newTitle, broadcastID, streamID, streamKey, ingestionAddress, youtube_url;
    private int maxqueue, statusCode;
    private Message<String> message;
    private ResponseEntity responseEntityStream;
    private ObjectNode objectEntityStream;

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

    public void initTask(long accountId, ESDevice device, boolean fromSchedule, String title, ObjectNode objectEntityStream) {
        this.accountId = accountId;
        this.device = device;
        this.fromSchedule = fromSchedule;
        this.title = title;
        this.objectEntityStream = objectEntityStream;
    }

    @Override
    public void run() {

        Calendar calendar = Calendar.getInstance();
        Date time;
        Date stopTime;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date).toString());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int minute = cal.get(Calendar.MINUTE);
        cal.add(Calendar.MINUTE, 5);
        String newTimePlusFive = dateFormat.format(cal.getTime());
        //round time for prolog
        Date dateHoursRound = toNearestWholeHour(date);

        System.out.println(newTimePlusFive);

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

            ResponseEntity responseYoutubeKey = accountCameraRepository.YoutubeKey(accountId);
            objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
            System.out.println("OBJECT KEY : " + objectKey);

            mqttTime = newTimePlusFive;
            newTitle = title + " " + mqttTime;

            responseEntityStream = prologVideo.runVideoProlog(newTitle, accountId);
            objectEntityStream = Json.parseToObjectNode(responseEntityStream.getBody().toString());

        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = calendar.getTime();

            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 5);
            stopTime = calendar.getTime();

            ResponseEntity responseYoutubeKey = accountCameraRepository.YoutubeKey(accountId);
            objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
            System.out.println("OBJECT KEY : " + objectKey);

            System.out.println("DATE HOURS ROUND : " + dateHoursRound);
            mqttTime = dateFormat.format(dateHoursRound).toString();
            newTitle = title + " " + mqttTime;
        }

        // PROLOG + TRANSITION EVENT
        accessToken = objectKey.get("access_token").textValue();
        clientId = objectKey.get("client_id").textValue();
        clientSecret = objectKey.get("client_secret").textValue();
        refreshToken = objectKey.get("refresh_token").textValue();
        maxqueue = objectKey.get("max_history").asInt();

        if (objectEntityStream.get("stream_data") != null) {
            System.out.println("OBJECT ENTITY STREAM : " + objectEntityStream);

            //DELETE OLD YOUTUBE LINK
            ResponseEntity cameraHistoryCount = historyCameraRepository.countRowHistoryCamera(device.getId());
            ObjectNode responseCamera = Json.parseToObjectNode(cameraHistoryCount.getBody().toString());
            System.out.println("responseCamera : " + responseCamera);
            if (responseCamera.size() != 0) {
                System.out.println("COUNT ROW HISTORY : " + responseCamera.get("count").toString() + " FIRST ID YOUTUBE : " + responseCamera.get("youtube_id").toString());
                if (Integer.parseInt(responseCamera.get("count").toString()) > maxqueue) {

                    //DELETE FIRST ROW HISTORY CAMERA
                    historyCameraRepository.deleteFirstRowByDeviceId(device.getId());

                    //DELETE ON YOUTUBE API
                    ResponseEntity responseEntityDelete = youtubeService.deleteEventById(accessToken, responseCamera.get("youtube_id").toString());
                    ObjectNode responseBodyDelete = Json.parseToObjectNode(responseEntityDelete.getBody().toString());
                    statusCode = Integer.parseInt(responseBodyDelete.get("status_code").textValue());
                    System.out.println(statusCode);

                    if (responseBodyDelete.get("status_code") != null && statusCode == 401) {
                        System.out.println("UNAUTHORIZED");
                        //get access token by Refresh token
                        accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, accountId);
                        responseEntityDelete = youtubeService.deleteEventById(accessToken, responseCamera.get("youtube_id").toString());
                        responseBodyDelete = Json.parseToObjectNode(responseEntityDelete.getBody().toString());
                        System.out.println("RESPONSE DELETE : " + responseBodyDelete);

                    }

                }
            }


            //INSERT INTO TABlE camera_history_tbl
            System.out.println("response success : " + objectEntityStream.get("stream_data"));

            String youtube_title = objectEntityStream.get("stream_data").get("data").get("title").textValue();

            try {
                broadcastID = objectEntityStream.get("stream_data").get("data").get("broadcast_id").textValue();
                streamID = objectEntityStream.get("stream_data").get("data").get("stream_id").textValue();
                streamKey = objectEntityStream.get("stream_data").get("data").get("stream_key").textValue();
                ingestionAddress = objectEntityStream.get("stream_data").get("data").get("ingestion_address").textValue();
                youtube_url = "https://youtu.be/" + broadcastID;
            } catch (NullPointerException e) {
                System.out.println("error : " + e.getMessage());

            }

            try {
                ESCameraHistory cameraHistory = new ESCameraHistory(youtube_title, youtube_url, broadcastID, streamID,
                        ingestionAddress + "/" + streamKey, dateFormat.parse(mqttTime), device);

                deviceCameraHistoryJpaRepository.saveAndFlush(cameraHistory);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        //get old broadcast id
        ESDevice deviceList = deviceRepository.findByDeviceId(device.getId(), accountId);
        String info = deviceList.getInfo();
        ObjectNode objectInfo = Json.parseToObjectNode(info);

        //CREATE MQTT RESPONSE JSON
        ObjectNode responseMqttJson = Json.buildObjectNode();
//            responseMqttJson.put("cid",device.getId());
        if (objectInfo.get("ybid") != null) {
            oldBroadcastID = objectInfo.get("ybid").textValue();
            responseMqttJson.put("yobid", oldBroadcastID);
        }
        responseMqttJson.put("tm", mqttTime);
        responseMqttJson.put("ysid", streamID);
        responseMqttJson.put("ysk", ingestionAddress + "/" + streamKey);
        responseMqttJson.put("ybid", broadcastID);
        responseMqttJson.put("yurl", youtube_url);

        //MQTT MESSAGE
        this.message = MessageBuilder
                .withPayload(responseMqttJson.toString())
                .setHeader(MqttHeaders.TOPIC,
                        "command/" + accountId + "/stream/" + device.getId())
                .setHeader(MqttHeaders.RETAINED, true)
                .setHeader(MqttHeaders.QOS, 2)
                .build();

        if (applicationEventPublisher != null && message != null) {
            applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
        } else {
            System.out.println("MQTT NULL");
        }

        //STOP PROLOG VIDEO
        System.out.println(objectEntityStream);
        if(objectEntityStream.get("stream_status").get("data")!=null){
            String streamStatus = objectEntityStream.get("stream_status").get("data").get("stream_status").textValue();
            if (streamStatus.equalsIgnoreCase("live")) {
                prologVideo.stopVideoProlog();
                deviceRepository.updateStatusInfoDevice(device.getId(), broadcastID, ingestionAddress, streamKey, streamID, youtube_url, mqttTime);
            }
        }

        System.out.println("broadcastID : " + broadcastID);
        System.out.println("streamID : " + streamID);
        System.out.println("streamKey : " + streamKey);


        // Add stop schedule
//        Calendar calendar1 = Calendar.getInstance();
//        calendar1.add(Calendar.MINUTE,1);
//        stopTime = calendar1.getTime();
        cameraManager.updateStopSchedule(accountId, device.getId(), broadcastID, stopTime, streamID);
    }
}
