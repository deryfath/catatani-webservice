package io.iotera.emma.smarthome.routine;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ConcurrentHashMap;

public class RoutineManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ConcurrentHashMap<String, LatchWithResult<Boolean>> scheduleLatches =
            new ConcurrentHashMap<String, LatchWithResult<Boolean>>();

    ///////////
    // Latch //
    ///////////
    private ConcurrentHashMap<Long, RoutineControlSchedule> schedules = new ConcurrentHashMap<Long,
            RoutineControlSchedule>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    LatchWithResult<Boolean> buildLatch(String routineId) {
        if (scheduleLatches.containsKey(routineId)) {
            return scheduleLatches.get(routineId);
        }

        LatchWithResult<Boolean> latch = LatchWithResult.create(Boolean.FALSE);
        scheduleLatches.put(routineId, latch);

        return latch;
    }

    LatchWithResult<Boolean> getLatch(String routineId) {
        if (!scheduleLatches.containsKey(routineId)) {
            return null;
        }

        return scheduleLatches.get(routineId);
    }

    //////////////
    // Schedule //
    //////////////

    boolean removeLatch(String routineId) {
        if (!scheduleLatches.containsKey(routineId)) {
            return false;
        }

        scheduleLatches.remove(routineId);
        return true;
    }

    private RoutineControlSchedule getSchedule(long hubId) {
        if (schedules.containsKey(hubId)) {
            return schedules.get(hubId);
        }

        RoutineControlSchedule schedule = applicationContext.getBean(RoutineControlSchedule.class);
        schedule.initSchedule(hubId);
        schedules.put(hubId, schedule);

        return schedule;
    }

    public int getActiveScheduleCount(long hubId) {
        RoutineControlSchedule schedule = getSchedule(hubId);
        return schedule.getActiveScheduleCount();
    }

    public boolean putSchedule(long hubId, ESRoutine routine, String cronExpression) {
        RoutineControlSchedule schedule = getSchedule(hubId);
        return schedule.putSchedule(routine, cronExpression);
    }

    public boolean removeSchedule(long hubId, String routineId) {
        RoutineControlSchedule schedule = getSchedule(hubId);
        return schedule.removeSchedule(routineId);
    }

    public boolean updateSchedule(long hubId, ESRoutine routine, String cronExpression) {
        RoutineControlSchedule schedule = getSchedule(hubId);
        return schedule.updateSchedule(routine, cronExpression);
    }

}
