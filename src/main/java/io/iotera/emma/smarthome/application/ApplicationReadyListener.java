package io.iotera.emma.smarthome.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.util.RoutineUtility;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ESRoutineRepo routineRepo;

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    RoutineManager routineManager;

    @Autowired
    CameraManager cameraManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

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
            ObjectNode info = Json.parseToObjectNode(camera.getInfo());

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

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tm);
            calendar.add(Calendar.HOUR, 1);
            calendar.add(Calendar.MINUTE, 5);
            Date time1 = calendar.getTime();

            YoutubeItem youtubeItem1 = new YoutubeItem(ybid, ysid, ysk, yurl);
            youtubeItem1.setTime(tm);

            Date time2 = null;
            YoutubeItem youtubeItem2 = null;

            if (Json.isObjectNodeHavingAllKeys(info, "yobid", "yosid", "yosk", "yourl", "otm")) {
                String yobid = info.get("yobid").asText("");
                String yosid = info.get("yosid").asText("");
                String yosk = info.get("yosk").asText("");
                String yourl = info.get("yourl").asText("");
                String otmString = info.get("otm").asText("");

                Date otm;
                try {
                    otm = sdf.parse(otmString);

                    calendar = Calendar.getInstance();
                    calendar.setTime(otm);
                    calendar.add(Calendar.HOUR, 1);
                    calendar.add(Calendar.MINUTE, 5);
                    Date otmStop = calendar.getTime();

                    if (now.compareTo(otmStop) < 0) {
                        youtubeItem2 = new YoutubeItem(yobid, yosid, yosk, yourl);
                        youtubeItem2.setTime(otm);
                        time2 = otmStop;
                    }

                } catch (ParseException e) {
                }
            }

            cameraManager.putScheduleOnApplicationReady(hubId, cameraId,
                    time1, youtubeItem1, time2, youtubeItem2);

        }

    }
}
