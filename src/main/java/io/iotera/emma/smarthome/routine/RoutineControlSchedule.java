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
import java.util.concurrent.Future;

@Component
@Scope("prototype")
public class RoutineControlSchedule implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private long accountId;
    private ThreadPoolTaskScheduler taskScheduler;
    private ConcurrentHashMap<String, Future> futures;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void initSchedule(long accountId) {
        this.accountId = accountId;
        this.futures = new ConcurrentHashMap<String, Future>();
    }

    boolean putSchedule(ESRoutine routine, String cronExpression) {
        if (this.taskScheduler == null && this.futures.isEmpty()) {
            this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("routineThreadPoolTaskScheduler");
        }

        if (!this.futures.containsKey(routine.getId())) {
            ScheduleTask task = applicationContext.getBean(ScheduleTask.class);
            task.initTask(this.accountId, routine.getId(), routine.getCategory(),
                    routine.getCommands(), routine.getClients());

            Future future = this.taskScheduler.schedule(
                    task,
                    new CronTrigger(cronExpression));
            this.futures.put(routine.getId(), future);

            return true;
        }

        return false;
    }

    boolean removeSchedule(String routineId) {
        if (this.futures.containsKey(routineId)) {
            Future future = this.futures.get(routineId);
            future.cancel(true);
            this.futures.remove(routineId);

            if (this.futures.isEmpty()) {
                this.taskScheduler.destroy();
                this.taskScheduler = null;
            }

            return true;
        }

        return false;
    }

    boolean updateSchedule(ESRoutine routine, String cronExpression) {
        // Remove existing schedule
        if (this.futures.containsKey(routine.getId())) {
            Future future = this.futures.get(routine.getId());
            future.cancel(true);
            this.futures.remove(routine.getId());
        }

        // Put new schedule
        putSchedule(routine, cronExpression);

        return true;
    }

    int getActiveScheduleCount() {
        return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
    }


}
