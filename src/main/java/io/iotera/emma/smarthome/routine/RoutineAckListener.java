package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RoutineAckListener implements ApplicationListener<RoutineAckEvent> {

    @Autowired
    RoutineManager routineManager;

    @Override
    public void onApplicationEvent(RoutineAckEvent event) {

        ObjectNode payload = event.getPayload();
        if (!payload.has("rid")) {
            return;
        }
        String routineId = payload.get("rid").textValue().trim();

        LatchWithResult<Boolean> latch = routineManager.getLatch(routineId);
        if (latch != null) {
            latch.setResult(true);
        }
    }

}
