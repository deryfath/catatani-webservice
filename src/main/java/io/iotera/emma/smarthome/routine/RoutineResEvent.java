package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.ApplicationEvent;

public class RoutineResEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private long accountId;
    private ObjectNode payload;

    public RoutineResEvent(Object source, long accountId, ObjectNode payload) {
        super(source);
        this.accountId = accountId;
        this.payload = payload;
    }

    public long getAccountId() {
        return accountId;
    }

    public ObjectNode getPayload() {
        return payload;
    }
}
