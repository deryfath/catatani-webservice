package io.iotera.emma.smarthome.routine;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ConcurrentHashMap;

public class RoutineManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    ///////////
    // Latch //
    ///////////

    private ConcurrentHashMap<String, LatchWithResult<Boolean>> scheduleLatchs =
            new ConcurrentHashMap<String, LatchWithResult<Boolean>>();

    public LatchWithResult<Boolean> buildLatch(String routineId) {
        if (scheduleLatchs.containsKey(routineId)) {
            return scheduleLatchs.get(routineId);
        }

        LatchWithResult<Boolean> latch = LatchWithResult.create(Boolean.FALSE);
        scheduleLatchs.put(routineId, latch);

        return latch;
    }

    public LatchWithResult<Boolean> getLatch(String routineId) {
        if (!scheduleLatchs.containsKey(routineId)) {
            return null;
        }

        return scheduleLatchs.get(routineId);
    }

    public boolean removeLatch(String routineId) {
        if (!scheduleLatchs.containsKey(routineId)) {
            return false;
        }

        scheduleLatchs.remove(routineId);
        return true;
    }

    //////////////
    // Schedule //
    //////////////

    private ConcurrentHashMap<Long, RoutineControlSchedule> schedulers = new ConcurrentHashMap<Long,
            RoutineControlSchedule>();

    private RoutineControlSchedule getSchedule(long accountId) {
        if (schedulers.containsKey(accountId)) {
            return schedulers.get(accountId);
        }

        RoutineControlSchedule schedule = applicationContext.getBean(RoutineControlSchedule.class);
        schedule.initSchedule(accountId);
        schedulers.put(accountId, schedule);

        return schedule;
    }

    public int getActiveScheduleCount(long accountId) {
        RoutineControlSchedule schedule = getSchedule(accountId);
        return schedule.getActiveScheduleCount();
    }

    public boolean putSchedule(long accountId, ESRoutine routine, String cronExpression) {
        RoutineControlSchedule schedule = getSchedule(accountId);
        return schedule.putSchedule(routine, cronExpression);
    }

    public boolean removeSchedule(long accountId, String routineId) {
        RoutineControlSchedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(routineId);
    }

    public boolean updateSchedule(long accountId, ESRoutine routine, String cronExpression) {
        RoutineControlSchedule schedule = getSchedule(accountId);
        return schedule.updateSchedule(routine, cronExpression);
    }

}
