package io.iotera.emma.smarthome.routine;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;

public class RoutineManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private RoutineManager getScheduleManager() {
        return this;
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

    private ConcurrentHashMap<Long, Schedule> schedulers = new ConcurrentHashMap<Long, Schedule>();

    private Schedule getSchedule(long accountId) {
        if (schedulers.containsKey(accountId)) {
            return schedulers.get(accountId);
        }

        Schedule schedule = new Schedule(accountId);
        schedulers.put(accountId, schedule);

        return schedule;
    }

    public int getActiveScheduleCount(long accountId) {
        Schedule schedule = getSchedule(accountId);
        return schedule.getActiveScheduleCount();
    }

    public boolean putSchedule(long accountId, ESRoutine routine, String cronExpression) {
        Schedule schedule = getSchedule(accountId);
        return schedule.putSchedule(routine, cronExpression);
    }

    public boolean removeSchedule(long accountId, String routineId) {
        Schedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(routineId);
    }

    public boolean updateSchedule(long accountId, ESRoutine routine, String cronExpression) {
        Schedule schedule = getSchedule(accountId);
        return schedule.updateSchedule(routine, cronExpression);
    }

    private class Schedule {

        private long accountId;

        private ThreadPoolTaskScheduler taskScheduler;
        private ConcurrentHashMap<String, ScheduledFuture> scheduleFutures;

        private Schedule(long accountId) {
            this.accountId = accountId;
            this.scheduleFutures = new ConcurrentHashMap<String, ScheduledFuture>();
        }

        private boolean putSchedule(ESRoutine routine, String cronExpression) {
            if (this.scheduleFutures.isEmpty() && this.taskScheduler == null) {
                this.taskScheduler = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            if (!this.scheduleFutures.containsKey(routine.getId())) {
                ScheduleTask task = applicationContext.getBean(ScheduleTask.class);
                task.setTask(getScheduleManager(), this.accountId, routine.getId(), routine.getCategory(),
                        routine.getCommands(), routine.getClients());

                ScheduledFuture scheduledFuture = this.taskScheduler.schedule(
                        task,
                        new CronTrigger(cronExpression));
                this.scheduleFutures.put(routine.getId(), scheduledFuture);

                return true;
            }

            return false;
        }

        private boolean removeSchedule(String routineId) {
            if (this.scheduleFutures.containsKey(routineId)) {
                ScheduledFuture scheduledFuture = this.scheduleFutures.get(routineId);
                scheduledFuture.cancel(true);
                this.scheduleFutures.remove(routineId);

                if (this.scheduleFutures.isEmpty()) {
                    this.taskScheduler.destroy();
                    this.taskScheduler = null;
                }

                return true;
            }

            return false;
        }

        private boolean updateSchedule(ESRoutine routine, String cronExpression) {
            // Remove existing schedule
            if (this.scheduleFutures.containsKey(routine.getId())) {
                ScheduledFuture scheduledFuture = this.scheduleFutures.get(routine.getId());
                scheduledFuture.cancel(true);
                this.scheduleFutures.remove(routine.getId());
            }

            // Put new schedule
            putSchedule(routine, cronExpression);

            return true;
        }

        private int getActiveScheduleCount() {
            return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
        }

    }
}
