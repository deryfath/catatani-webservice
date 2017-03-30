package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.ApplicationEvent;

public class RoutineResEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private long accountId;
    private String routineId;
    private ObjectNode payload;

    public RoutineResEvent(Object source, long accountId, String routineId, ObjectNode payload) {
        super(source);
        this.accountId = accountId;
        this.routineId = routineId;
        this.payload = payload;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getRoutineId() {
        return routineId;
    }

    public ObjectNode getPayload() {
        return payload;
    }
}
