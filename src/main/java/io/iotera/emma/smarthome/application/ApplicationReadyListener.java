package io.iotera.emma.smarthome.application;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.util.RoutineUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ESRoutineRepo routineRepo;

    @Autowired
    RoutineManager routineManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        // Add Schedule
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

        //cameraManager.putSchedule(1, "id-1");
    }
}
