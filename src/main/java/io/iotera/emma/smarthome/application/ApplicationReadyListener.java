package io.iotera.emma.smarthome.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.emma.smarthome.util.RoutineUtility;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent>, ApplicationEventPublisherAware {

    @Autowired
    ESRoutineRepo routineRepo;

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

    @Autowired
    RoutineManager routineManager;

    @Autowired
    CameraManager cameraManager;

    @Autowired
    YoutubeService youtubeService;

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

//        for (int i=0; i<20; i++){
//            String accessToken = "ya29.GlwzBB9qOA4_Ucr_Y-xTXl7feQW9bzzcNkCPa9pQAEQQc_QnfUff1ZbYGnZUj-GNthb3ILJQYaMuS-N-4vOwWJ0mEQGYMXr0wsDS54Ri8JUHmu6yjNqpeRZPI2Knaw";
//            youtubeService.createStream(accessToken);
//
//            try {
//                Thread.sleep(1000);
//            }catch (InterruptedException e){
//
//            }
//
//            System.out.println("test");
//        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();

        // Activate all Schedule
        List<ESRoutine> routines = routineRepo.listActiveSchedule();
        for (ESRoutine routine : routines) {
            long hubId = routine.getHubId();
            String cronExpression = RoutineUtility.getCronExpression(routine.getDaysOfWeek(),
                    routine.getTrigger());
            boolean valid = RoutineUtility.getValidCommands(routine.getCommands()) != null;

            if (hubId != -1 && cronExpression != null && valid) {
                routineManager.putSchedule(hubId, routine, cronExpression);
            }
        }

        // Activate all Camera
        List<ESDevice> cameras = deviceRepo.listCamera();
        for (ESDevice camera : cameras) {
            long hubId = camera.getHubId();
            String cameraId = camera.getId();
            String infoString = camera.getInfo();

            List<Tuple.T2<Date, YoutubeItem>> stopSchedules = new ArrayList<Tuple.T2<Date, YoutubeItem>>();

            // Obtain old info
            ObjectNode info = Json.parseToObjectNode(infoString);
            if (!Json.isObjectNodeHavingAllKeys(info, "ybid", "ysid", "ysk", "yurl", "tm")) {
                continue;
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
                continue;
            }

            YoutubeItem youtubeItem = new YoutubeItem(ybid, ysid, ysk, yurl);
            youtubeItem.setTime(tm);

            ObjectNode payload = youtubeItem.getInfo();
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

            List<ESCameraHistory> histories = cameraHistoryRepo.listIncompleteHistoryByCameraId(cameraId, hubId);
            for (ESCameraHistory history : histories) {

                youtubeItem = new YoutubeItem(history.getYoutubeBroadcastId(), history.getYoutubeStreamId(),
                        history.getYoutubeStreamKey(), history.getYoutubeUrl());
                youtubeItem.setTime(history.getHistoryTime());

                calendar.setTime(history.getHistoryTime());
                calendar.add(Calendar.HOUR, 1);
                calendar.add(Calendar.MINUTE, 5);
                Date stopTime = calendar.getTime();

                stopSchedules.add(new Tuple.T2<Date, YoutubeItem>(
                        stopTime, youtubeItem
                ));
            }

            cameraManager.putScheduleOnApplicationReady(hubId, cameraId, stopSchedules);
        }

    }
}
