package io.iotera.emma.smarthome.application;

import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.util.RoutineUtility;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    ESCameraHistoryRepo cameraHistoryRepo;

    @Autowired
    RoutineManager routineManager;

    @Autowired
    CameraManager cameraManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

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
            List<Tuple.T2<Date, YoutubeItem>> stopSchedules = new ArrayList<Tuple.T2<Date, YoutubeItem>>();

            List<ESCameraHistory> histories = cameraHistoryRepo.listIncompleteHistoryByCameraId(cameraId, hubId);
            for (ESCameraHistory history : histories) {

                YoutubeItem youtubeItem = new YoutubeItem(history.getYoutubeBroadcastId(), history.getYoutubeStreamId(),
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
