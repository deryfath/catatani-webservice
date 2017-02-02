package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.camera.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepository;
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
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

/**
 * Created by nana on 12/14/2016.
 */

@Component
@Scope("prototype")
public class ScheduleTaskYoutube implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    Environment env;

    int counter = 1;
    int countdown = 6;
    String broadcastID, streamID, streamKey;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESCameraHistoryRepository.ESCameraHistoryJpaRepository deviceCameraHistoryJpaRepository;

    @Autowired
    ESCameraHistoryRepository historyCameraRepository;

    @Autowired
    PrologVideo prologVideo;

    @Autowired
    ESDeviceRepository deviceRepository;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    private RoutineManagerYoutube routineManager;
    private long accountId;
    private String routineId;
    private Message<String> message;
    private ObjectNode objectKey;
    private String title;
    private String refreshToken = "";
    private String clientId = "";
    private String clientSecret = "";
    private String accessToken = "";
    private int statusCode;
    private String state;
    private int counterNodata = 0;
    private String stateTask;
    private ESDevice device;
    private int maxqueue;
    private String youtube_id;
    private String newTitle = "";

    private volatile boolean running = true;

    public void setTask(RoutineManagerYoutube routineManager, long accountId,
                        ObjectNode objectKey, String title, String stateTask, ESDevice device, int maxqueue) {
        this.routineManager = routineManager;
        this.accountId = accountId;
        this.routineId = routineId;
        this.objectKey = objectKey;
        this.title = title;
        this.stateTask = stateTask;
        this.device = device;
        this.maxqueue = maxqueue;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void run() {

        accessToken = objectKey.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientId = objectKey.get("client_id").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientSecret = objectKey.get("client_secret").toString().replaceAll("[^\\w\\s\\-_.]", "");
        refreshToken = objectKey.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");

        boolean sent = false;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date).toString());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int minute = cal.get(Calendar.MINUTE);
        cal.add(Calendar.MINUTE, 5);
        String newTimePlusFive = dateFormat.format(cal.getTime());

        System.out.println(newTimePlusFive);

        while (running) {
            System.out.println("CREATE EVENT " + stateTask);
//            ESHubAccountCameraController responseEntity = youtubeService.createEvent(accessToken,title+" "+newTimePlusFive);
//            ObjectNode responseBody = Json.parseToObjectNode(responseEntity.getBody().toString());
//
//            statusCode = Integer.parseInt(responseBody.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
//            System.out.println(statusCode);
//
//            if(responseBody.get("status_code") != null && statusCode == 401){
//                System.out.println("UNAUTHORIZED");
//                //get access token by Refresh token
//                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret);
//                responseEntity = youtubeService.createEvent(accessToken,title+" "+newTimePlusFive);
//                responseBody = Json.parseToObjectNode(responseEntity.getBody().toString());
//
//            }

            if (stateTask.equalsIgnoreCase("taskProlog")) {
                newTitle = title + " Prolog " + dateFormat.format(date).toString();
            } else {
                newTitle = title + " " + newTimePlusFive;
            }

            ResponseEntity responseEntityStream = prologVideo.runVideoProlog(newTitle, accountId);
            ObjectNode objectEntityStream = Json.parseToObjectNode(responseEntityStream.getBody().toString());

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
                        statusCode = Integer.parseInt(responseBodyDelete.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
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

                String youtube_url = objectEntityStream.get("stream_data").get("data").get("link").toString().replaceAll("[^\\w\\s\\-/:.?&]", "");
                String youtube_title = objectEntityStream.get("stream_data").get("data").get("title").toString().replaceAll("[^\\w\\s\\-:]", "");
                Date datePublish = null;

                try {
                    datePublish = dateFormat.parse(dateFormat.format(date).toString());
                    System.out.println("datePublish : " + datePublish);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try{
                    broadcastID = objectEntityStream.get("stream_data").get("data").get("broadcast_id").toString().replaceAll("[^\\w\\s\\-_]", "");
                    streamID = objectEntityStream.get("stream_data").get("data").get("stream_id").toString().replaceAll("[^\\w\\s\\-_]", "");
                    streamKey = objectEntityStream.get("stream_data").get("data").get("stream_key").toString().replaceAll("[^\\w\\s\\-]", "");
                }catch (NullPointerException e){
                    running = false;
                    System.out.println("error : "+e.getMessage());
                    break;
                }

                ESCameraHistory cameraHistory = new ESCameraHistory(youtube_title, youtube_url, broadcastID, streamID,
                        streamKey, datePublish, device);
                deviceCameraHistoryJpaRepository.saveAndFlush(cameraHistory);
            }

            objectEntityStream.put("device_id", device.getId());

            //MQTT MESSAGE
            this.message = MessageBuilder
                    .withPayload(objectEntityStream.toString())
                    .setHeader(MqttHeaders.TOPIC,
                            "command/" + accountId + "/stream")
                    .build();

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
            } else {
                break;
            }

            //STOP PROLOG VIDEO
            String streamStatus = objectEntityStream.get("stream_status").get("data").get("stream_status").toString().replaceAll("[^\\w\\s]", "");
            if (streamStatus.equalsIgnoreCase("liveStarting")) {
                prologVideo.stopVideoProlog();
                deviceRepository.updateStatusInfoDevice(device.getId(), youtube_id);
            }

            try {
                broadcastID = objectEntityStream.get("stream_data").get("data").get("broadcast_id").toString().replaceAll("[^\\w\\s\\-_]", "");
                streamID = objectEntityStream.get("stream_data").get("data").get("stream_id").toString().replaceAll("[^\\w\\s\\-_]", "");
                streamKey = objectEntityStream.get("stream_data").get("data").get("stream_key").toString().replaceAll("[^\\w\\s\\-]", "");
            } catch (NullPointerException e) {
                running = false;
                System.out.println("error : " + e.getMessage());
                break;
            }

            System.out.println("broadcastID : " + broadcastID);
            System.out.println("streamID : " + streamID);
            System.out.println("streamKey : " + streamKey);

//            try {
//                Thread.sleep(60000);
//            }catch (InterruptedException ex){
//                System.out.println(ex.getMessage());
//            }
//
//
//            //TRANSITION TESTING -> LIVE
//            System.out.println("START EVENT "+stateTask);
//
//            state = "testing";
//            ESHubAccountCameraController responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);
//
//            ObjectNode responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());
//
//            statusCode = Integer.parseInt(responseBodyTransitionStart.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
//            System.out.println(statusCode);
//
//            if(responseBodyTransitionStart.get("status_code") != null && statusCode == 401){
//                System.out.println("UNAUTHORIZED");
//                //get access token by Refresh token
//                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret);
//                responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);
//                responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());
//
//            }
//
//
//            //MQTT MESSAGE IF NO DATA
//            String statusNodata = responseBodyTransitionStart.get("data").get("stream_status").toString().replaceAll("[^\\w\\s]", "");
//            if(statusNodata.equalsIgnoreCase("noData")){
//
//                while(statusNodata.equalsIgnoreCase("noData")){
//
//                    responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);
//                    responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());
//
//                    this.message = MessageBuilder
//                            .withPayload(responseBodyTransitionStart.toString())
//                            .setHeader(MqttHeaders.TOPIC,
//                                    "stream/transition/start")
//                            .build();
//
//                    if (applicationEventPublisher != null && message != null) {
//                        applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
//                    } else {
//                        break;
//                    }
//
//                    statusNodata = responseBodyTransitionStart.get("data").get("stream_status").toString().replaceAll("[^\\w\\s]", "");
//                    if(!statusNodata.equalsIgnoreCase("noData")){
//                        break;
//                    }
//                }
//
//            }

//            //MQTT MESSAGE IF SUCCESS
//            this.message = MessageBuilder
//                    .withPayload(responseBodyTransitionStart.toString())
//                    .setHeader(MqttHeaders.TOPIC,
//                            "stream/transition/start")
//                    .build();
//
//            if (applicationEventPublisher != null && message != null) {
//                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
//            } else {
//                break;
//            }
//
//            try{
//                String dataStart = responseBodyTransitionStart.get("data").toString();
//                System.out.println(dataStart);
//            }catch (NullPointerException e){
//                running = false;
//                System.out.println(e.getMessage());
//                break;
//            }


            //MAKE TIMER TO Stop Stream
            try {
//                Thread.sleep(300); // 58 minute

                if (stateTask.equalsIgnoreCase("taskProlog")) {
                    int additionalMinuteThreadProlog = 0;
                    if (minute > 55) {
                        additionalMinuteThreadProlog = ((60 - minute) + 55) * 60 * 1000;
                    } else if (minute < 55) {
                        additionalMinuteThreadProlog = (55 - minute) * 60 * 1000;
                    }

                    int threadProlog = 4200000 + additionalMinuteThreadProlog;
                    System.out.println("THREAD PROLOG : " + additionalMinuteThreadProlog);
                    System.out.println("THREAD PROLOG : " + threadProlog);
                    Thread.sleep(threadProlog);
                } else {
                    Thread.sleep(4200000); // 70 minute
                }

            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("STOP EVENT " + stateTask);
            state = "complete";
            ResponseEntity responseTransitionStop = youtubeService.transitionEvent(accessToken, broadcastID, streamID, state);

            ObjectNode responseBodyTransitionStop = Json.parseToObjectNode(responseTransitionStop.getBody().toString());

            statusCode = Integer.parseInt(responseBodyTransitionStop.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
            System.out.println(statusCode);

            if (responseBodyTransitionStop.get("status_code") != null && statusCode == 401) {
                System.out.println("UNAUTHORIZED");
                //get access token by Refresh token
                System.out.println("CLIENT ID STOP : " + clientId);
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, accountId);
                System.out.println("stop access token : " + accessToken);
                responseTransitionStop = youtubeService.transitionEvent(accessToken, broadcastID, streamID, state);
                responseBodyTransitionStop = Json.parseToObjectNode(responseTransitionStop.getBody().toString());

            }

            //MQTT MESSAGE
            this.message = MessageBuilder
                    .withPayload(responseBodyTransitionStop.toString())
                    .setHeader(MqttHeaders.TOPIC,
                            "stream/transition/stop")
                    .build();

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
            } else {
                break;
            }

            try {
                String dataStop = responseBodyTransitionStop.get("data").toString();
                System.out.println(dataStop);
            } catch (NullPointerException e) {
                running = false;
                System.out.println(e.getMessage());
                break;
            }

            --countdown;
            break;
        }

        routineManager.removeSchedule(accountId, routineId, stateTask, device);
        routineManager.updateScheduleContinue(device, accountId, objectKey, title, stateTask);


        System.out.println("Scheduling over");

    }
}


