package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.util.Json;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by nana on 12/14/2016.
 */
public class RoutineManagerYoutube implements ApplicationContextAware {

    @Autowired
    ESDeviceRepository deviceRepository;

    private ApplicationContext applicationContext;
    private int maxqueue;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private RoutineManagerYoutube getScheduleManager() {
        return this;
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

    public boolean removeSchedule(long accountId, String routineId, String stateTask,ESDevice device) {
        Schedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(routineId,stateTask,device);
    }

    public boolean updateSchedule(ESDevice device, long accountId, ObjectNode ObjectKey, String title, int maxqueue) {
        Schedule schedule = getSchedule(accountId);
        this.maxqueue = maxqueue;
        return schedule.updateSchedule(device, accountId, ObjectKey, title, maxqueue);
    }

    public boolean updateScheduleContinue(ESDevice device, long accountId, ObjectNode ObjectKey, String title, String stateTask) {
        Schedule schedule = getSchedule(accountId);
        return schedule.updateScheduleContinue(device,accountId, ObjectKey, title, stateTask);
    }

    private class Schedule {

        private long accountId;

        private ThreadPoolTaskScheduler taskSchedulerProlog;
        private ThreadPoolTaskScheduler taskScheduler;
        private ThreadPoolTaskScheduler taskScheduler2;
        private ConcurrentHashMap<String, ScheduledFuture> scheduleFuturesProlog;
        private ConcurrentHashMap<String, ScheduledFuture> scheduleFutures;
        private ConcurrentHashMap<String, ScheduledFuture> scheduleFutures2;
        private ScheduledFuture scheduledFutureProlog,scheduledFuture,scheduledFuture2;
        private ScheduleTaskYoutube taskProlog = new ScheduleTaskYoutube();
        private ScheduleTaskYoutube task = new ScheduleTaskYoutube();
        private ScheduleTaskYoutube task2 = new ScheduleTaskYoutube();

        private Schedule(long accountId) {
            this.accountId = accountId;
            this.scheduleFuturesProlog = new ConcurrentHashMap<String, ScheduledFuture>();
            this.scheduleFutures = new ConcurrentHashMap<String, ScheduledFuture>();
            this.scheduleFutures2 = new ConcurrentHashMap<String, ScheduledFuture>();
        }

        private boolean putSchedule(ESDevice device, long accountId, ObjectNode objectKey, String title, int maxqueue) {
            if (this.scheduleFuturesProlog.isEmpty() && this.taskSchedulerProlog == null ) {
                this.taskSchedulerProlog = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            if (this.scheduleFutures.isEmpty() && this.taskScheduler == null ) {
                this.taskScheduler = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            if (this.scheduleFutures2.isEmpty() && this.taskScheduler2 == null) {
                this.taskScheduler2 = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            System.out.println(minute);
            System.out.println(hours);

            //task prolog
            if(minute != 55){
                String cronProlog = "* "+minute+" "+hours+" * * ?";
                System.out.println("MASUK THREAD PROLOG");
                System.out.println("cronProlog : "+cronProlog);
                taskProlog = applicationContext.getBean(ScheduleTaskYoutube.class);
                taskProlog.setTask(getScheduleManager(), accountId, objectKey, title,"taskProlog", device, maxqueue);

                scheduledFutureProlog = this.taskSchedulerProlog.schedule(
                        taskProlog,
                        new CronTrigger(cronProlog));

                this.scheduleFuturesProlog.put(String.valueOf(accountId), scheduledFutureProlog);
            }

            if(minute>55){
                hours++;
            }

            int hoursPlusTwo = hours + 2;

            String cronExpression = "* 55 "+hours+"-"+hoursPlusTwo+" * * ?";
//            String cronExpression = "0/10 * * * * ?";

            System.out.println("CRON EXP : "+cronExpression);

            //task 1

            System.out.println("MASUK THREAD 1");
            task = applicationContext.getBean(ScheduleTaskYoutube.class);
            task.setTask(getScheduleManager(), accountId, objectKey, title,"task1", device, maxqueue);

            scheduledFuture = this.taskScheduler.schedule(
                    task,
                    new CronTrigger(cronExpression));

            this.scheduleFutures.put(String.valueOf(accountId), scheduledFuture);


            //task 2

            System.out.println("MASUK THREAD 2");
            int hoursPlusOne = hours + 1;
            hoursPlusTwo = hoursPlusOne + 2;
//            cronExpression = "0/20 * * * * ?";
            cronExpression = "* 55 " + hoursPlusOne + "-" + hoursPlusTwo + " * * ?";
            System.out.println("CRON EXP2 : " + cronExpression);

            task2 = applicationContext.getBean(ScheduleTaskYoutube.class);
            task2.setTask(getScheduleManager(), accountId, objectKey, title, "task2", device, maxqueue);

            scheduledFuture2 = this.taskScheduler2.schedule(
                    task2,
                    new CronTrigger(cronExpression));

            this.scheduleFutures2.put(String.valueOf(accountId), scheduledFuture2);

            return true;
        }

        private boolean putScheduleContinue(ESDevice device, long accountId, ObjectNode ObjectKey, String title, String stateTask) {
            if (this.scheduleFutures.isEmpty() && this.taskScheduler == null ) {
                this.taskScheduler = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            if (this.scheduleFutures2.isEmpty() && this.taskScheduler2 == null) {
                this.taskScheduler2 = applicationContext.getBean(ThreadPoolTaskScheduler.class);
            }

            System.out.println("MAX QUEUE : "+maxqueue);

            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            System.out.println(minute);
            System.out.println(hours);

            if(minute>55){
                hours++;
            }

            int hoursPlusTwo = hours + 2;

            String cronExpression = "";

            //task 1
            if(stateTask.equalsIgnoreCase("task1")) {
                System.out.println("MASUK THREAD 1 CONTINUE");
                cronExpression = "0 55 "+hours+"-"+hoursPlusTwo+" * * ?";
                System.out.println("CRON EXP : "+cronExpression);
                task = applicationContext.getBean(ScheduleTaskYoutube.class);
                task.setTask(getScheduleManager(), accountId, ObjectKey, title, "task1",device,maxqueue);

                scheduledFuture = this.taskScheduler.schedule(
                        task,
                        new CronTrigger(cronExpression));

                this.scheduleFutures.put(String.valueOf(accountId), scheduledFuture);
            }

            //task 2
            if(stateTask.equalsIgnoreCase("task2")) {

                System.out.println("MASUK THREAD 2 CONTINUE");
                cronExpression = "0 55 " + hours + "-" + hoursPlusTwo + " * * ?";
                System.out.println("CRON EXP2 : " + cronExpression);

                task2 = applicationContext.getBean(ScheduleTaskYoutube.class);
                task2.setTask(getScheduleManager(), accountId, ObjectKey, title, "task2", device,maxqueue);

                scheduledFuture2 = this.taskScheduler2.schedule(
                        task2,
                        new CronTrigger(cronExpression));

                this.scheduleFutures2.put(String.valueOf(accountId), scheduledFuture2);
            }

            return true;
        }

        private boolean removeSchedule(String routineId, String stateTask, ESDevice device) {

            if(stateTask.equalsIgnoreCase("taskProlog")){
//                if (this.scheduleFutures.isEmpty()) {
                System.out.println("STOP THREAD PROLOG");
                this.scheduledFutureProlog.cancel(true);
                this.taskSchedulerProlog.destroy();
                this.taskSchedulerProlog = null;
                this.scheduleFuturesProlog.clear();
//                }
            }else if(stateTask.equalsIgnoreCase("task1")){
//                if (this.scheduleFutures.isEmpty()) {
                System.out.println("STOP THREAD 1");
                this.scheduledFuture.cancel(true);
                this.taskScheduler.destroy();
                this.taskScheduler = null;
                this.scheduleFutures.clear();
//                }
            }else if(stateTask.equalsIgnoreCase("task2")) {

//                if (!this.scheduleFutures2.isEmpty()) {
                System.out.println("STOP THREAD 2");
                this.scheduledFuture2.cancel(true);
                this.taskScheduler2.destroy();
                this.taskScheduler2 = null;
                this.scheduleFutures2.clear();
//                }
            }

            return false;
        }

        private boolean updateSchedule(ESDevice device, long accountId,  ObjectNode objectKey, String title, int maxqueue) {
            // Remove existing schedule
            if (this.scheduleFutures.containsKey(accountId)) {
                ScheduledFuture scheduledFuture = this.scheduleFutures.get(accountId);
                scheduledFuture.cancel(true);
                this.scheduleFutures.remove(accountId);
            }

            if (this.scheduleFutures2.containsKey(accountId)) {
                ScheduledFuture scheduledFuture2 = this.scheduleFutures2.get(accountId);
                scheduledFuture2.cancel(true);
                this.scheduleFutures2.remove(accountId);
            }

            // Put new schedule
            putSchedule(device, accountId, objectKey, title, maxqueue);

            return true;
        }

        private boolean updateScheduleContinue(ESDevice device, long accountId, ObjectNode ObjectKey, String title, String stateTask) {
            // Remove existing schedule
//            if (this.scheduleFutures.containsKey(accountId)) {
//                ScheduledFuture scheduledFuture = this.scheduleFutures.get(accountId);
//                scheduledFuture.cancel(true);
//                this.scheduleFutures.remove(accountId);
//            }
//
//            if (this.scheduleFutures2.containsKey(accountId)) {
//                ScheduledFuture scheduledFuture2 = this.scheduleFutures2.get(accountId);
//                scheduledFuture2.cancel(true);
//                this.scheduleFutures2.remove(accountId);
//            }

            // Put new schedule
            putScheduleContinue(device, accountId, ObjectKey, title, stateTask);

            return true;
        }

        private int getActiveScheduleCount() {
            return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
        }

    }
}
