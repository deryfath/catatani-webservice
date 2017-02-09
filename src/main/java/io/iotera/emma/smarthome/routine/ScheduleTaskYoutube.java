package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.regexp.internal.RE;
import io.iotera.emma.smarthome.model.camera.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepository;
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
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
import java.util.GregorianCalendar;

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
    private String ingestionAddress = "";
    private String mqttTime = "";
    private String youtube_url = "";
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

    static Date toNearestWholeHour(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.MINUTE) <= 59)
            c.set(Calendar.HOUR, c.get(Calendar.HOUR));

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
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

            //round time for prolog
            Date dateHoursRound = toNearestWholeHour(date);

            if (stateTask.equalsIgnoreCase("taskProlog")) {
                System.out.println("DATE HOURS ROUND : "+dateHoursRound);
                mqttTime = dateFormat.format(dateHoursRound).toString();
                newTitle = title + " " + mqttTime;
            } else {
                mqttTime = newTimePlusFive;
                newTitle = title + " " + mqttTime;
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
                Date datePublish = null;

//                try {
//                    datePublish = dateFormat.parse(dateFormat.format(date).toString());
//                    System.out.println("datePublish : " + datePublish);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                try{
                    broadcastID = objectEntityStream.get("stream_data").get("data").get("broadcast_id").textValue();
                    streamID = objectEntityStream.get("stream_data").get("data").get("stream_id").textValue();
                    streamKey = objectEntityStream.get("stream_data").get("data").get("stream_key").textValue();
                    ingestionAddress = objectEntityStream.get("stream_data").get("data").get("ingestion_address").textValue();
                    youtube_url = "https://youtu.be/"+broadcastID;
                }catch (NullPointerException e){
                    running = false;
                    System.out.println("error : "+e.getMessage());
                    break;
                }

                try {
                    ESCameraHistory cameraHistory = new ESCameraHistory(youtube_title, youtube_url, broadcastID, streamID,
                            ingestionAddress+"/"+streamKey, dateFormat.parse(mqttTime), device);

                    deviceCameraHistoryJpaRepository.saveAndFlush(cameraHistory);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            //CREATE MQTT RESPONSE JSON
            ObjectNode responseMqttJson = Json.buildObjectNode();
//            responseMqttJson.put("cid",device.getId());
            responseMqttJson.put("tm",mqttTime);
            responseMqttJson.put("ysid",streamID);
            responseMqttJson.put("ysk",ingestionAddress+"/"+streamKey);
            responseMqttJson.put("ybid",broadcastID);
            responseMqttJson.put("yurl",youtube_url);

            //MQTT MESSAGE
            this.message = MessageBuilder
                    .withPayload(responseMqttJson.toString())
                    .setHeader(MqttHeaders.TOPIC,
                            "command/" + accountId + "/stream/" + device.getId())
                    .setHeader(MqttHeaders.RETAINED,true)
                    .setHeader(MqttHeaders.QOS,2)
                    .build();

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
            } else {
                break;
            }

            //STOP PROLOG VIDEO
            String streamStatus = objectEntityStream.get("stream_status").get("data").get("stream_status").textValue();
            if (streamStatus.equalsIgnoreCase("live")) {
                prologVideo.stopVideoProlog();
                deviceRepository.updateStatusInfoDevice(device.getId(), broadcastID, ingestionAddress, streamKey, streamID, youtube_url, mqttTime);
            }

            System.out.println("broadcastID : "+broadcastID);
            System.out.println("streamID : "+streamID);
            System.out.println("streamKey : "+streamKey);

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
            Tuple.T2<Integer, ObjectNode> responseTransitionStop = youtubeService.transitionEventComplete(accessToken, broadcastID, streamID, state);

            if (responseTransitionStop._1 == 401) {
                System.out.println("UNAUTHORIZED");
                //get access token by Refresh token
                System.out.println("CLIENT ID STOP : " + clientId);
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, accountId);
                System.out.println("stop access token : " + accessToken);
                responseTransitionStop = youtubeService.transitionEventComplete(accessToken, broadcastID, streamID, state);

            }

            //MQTT MESSAGE
            this.message = MessageBuilder
                    .withPayload(responseTransitionStop._2.textValue())
                    .setHeader(MqttHeaders.TOPIC,
                            "stream/transition/stop")
                    .build();

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
            } else {
                break;
            }

            try {
                String dataStop = responseTransitionStop._2.textValue();
                System.out.println(dataStop);
            } catch (NullPointerException e) {
                running = false;
                System.out.println(e.getMessage());
                break;
            }

            --countdown;
            break;
        }

        routineManager.removeSchedule(accountId,stateTask,device);
        routineManager.updateScheduleContinue(device,accountId,objectKey,title,stateTask);


        System.out.println("Scheduling over");

    }
}



