package io.iotera.emma.smarthome.routine;

import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ScheduleAckListener implements ApplicationListener<ScheduleAckEvent> {

    @Autowired
    RoutineManager routineManager;

    @Override
    public void onApplicationEvent(ScheduleAckEvent event) {

        String routineId = event.getRoutineId();
        if (routineId == null) {
            return;
        }

        LatchWithResult<Boolean> latch = routineManager.getLatch(routineId);
        if (latch != null) {
            latch.setResult(true);
        }
    }

}
