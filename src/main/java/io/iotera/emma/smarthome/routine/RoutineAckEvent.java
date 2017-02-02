package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.ApplicationEvent;

public class RoutineAckEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private ObjectNode payload;

    public RoutineAckEvent(Object source, ObjectNode payload) {
        super(source);
        this.payload = payload;
    }

    public ObjectNode getPayload() {
        return payload;
    }
}
