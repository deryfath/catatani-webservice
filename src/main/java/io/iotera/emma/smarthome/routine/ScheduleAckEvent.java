package io.iotera.emma.smarthome.routine;

import org.springframework.context.ApplicationEvent;

public class ScheduleAckEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String routineId;

    public ScheduleAckEvent(Object source, String routineId) {
        super(source);
        this.routineId = routineId;
    }

    public String getRoutineId() {
        return routineId;
    }
}
