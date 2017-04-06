package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.ApplicationEvent;

public class RoutineResEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private long hubId;
    private String routineId;
    private ObjectNode payload;

    public RoutineResEvent(Object source, long hubId, String routineId, ObjectNode payload) {
        super(source);
        this.hubId = hubId;
        this.routineId = routineId;
        this.payload = payload;
    }

    public long getHubId() {
        return hubId;
    }

    public String getRoutineId() {
        return routineId;
    }

    public ObjectNode getPayload() {
        return payload;
    }
}
