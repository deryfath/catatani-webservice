package io.iotera.emma.smarthome.routine;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Scope("prototype")
public class RoutineControlSchedule implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private long accountId;

    private ThreadPoolTaskScheduler taskScheduler;
    private ConcurrentHashMap<String, ScheduledFuture> scheduleFutures;

    void initSchedule(long accountId) {
        this.accountId = accountId;
        this.scheduleFutures = new ConcurrentHashMap<String, ScheduledFuture>();
    }

    boolean putSchedule(ESRoutine routine, String cronExpression) {
        if (this.taskScheduler == null && this.scheduleFutures.isEmpty()) {
            this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("routineThreadPoolTaskScheduler");
        }

        if (!this.scheduleFutures.containsKey(routine.getId())) {
            ScheduleTask task = applicationContext.getBean(ScheduleTask.class);
            task.setTask(this.accountId, routine.getId(), routine.getCategory(),
                    routine.getCommands(), routine.getClients());

            ScheduledFuture scheduledFuture = this.taskScheduler.schedule(
                    task,
                    new CronTrigger(cronExpression));
            this.scheduleFutures.put(routine.getId(), scheduledFuture);

            return true;
        }

        return false;
    }

    boolean removeSchedule(String routineId) {
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

    boolean updateSchedule(ESRoutine routine, String cronExpression) {
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

    int getActiveScheduleCount() {
        return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
    }


}
