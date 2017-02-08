package io.iotera.emma.smarthome.application;

import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.repository.ESRoutineRepository;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.utility.RoutineUtility;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ESRoutineRepository routineRepository;

    @Autowired
    RoutineManager routineManager;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    CameraManager cameraManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        // Add Schedule
        List<ESRoutine> routines = routineRepository.listActiveSchedule();
        for (ESRoutine routine : routines) {
            long accountId = routine.getAccountId();
            String cronExpression = RoutineUtility.getCronExpression(routine.getDaysOfWeek(),
                    routine.getTrigger());
            boolean valid = RoutineUtility.getValidCommands(routine.getCommands()) != null;

            if (accountId != -1 && cronExpression != null && valid) {
                routineManager.putSchedule(accountId, routine, cronExpression);
            }
        }

        //cameraManager.putSchedule(1, "id-1");
    }
}
