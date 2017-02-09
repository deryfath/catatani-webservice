package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepository;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
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

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    @Autowired
    ESCameraHistoryRepository cameraHistoryRepository;

    @Autowired
    YoutubeService youtubeService;

    private ApplicationContext applicationContext;
    private int maxqueue;
    private boolean manualDelete = false;

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

    public boolean removeSchedule(long accountId, String stateTask, ESDevice device) {
        Schedule schedule = getSchedule(accountId);
        return schedule.removeSchedule(stateTask,device);
    }

    public boolean removeScheduleByDeviceId(long accountId, String deviceId) {
        this.manualDelete = true;
        Schedule schedule = getSchedule(accountId);
        return schedule.removeScheduleByDeviceId(deviceId,accountId);
    }

    public boolean updateSchedule(ESDevice device, long accountId, ObjectNode ObjectKey, String title) {
        Schedule schedule = getSchedule(accountId);
        this.maxqueue = ObjectKey.get("max_history").asInt();
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
        private Message<String> message;
        private volatile ApplicationEventPublisher applicationEventPublisher;

        private Schedule(long accountId) {
            this.accountId = accountId;
            this.scheduleFuturesProlog = new ConcurrentHashMap<String, ScheduledFuture>();
            this.scheduleFutures = new ConcurrentHashMap<String, ScheduledFuture>();
            this.scheduleFutures2 = new ConcurrentHashMap<String, ScheduledFuture>();
        }

        private boolean putSchedule(ESDevice device, long accountId, ObjectNode objectKey, String title, int maxqueue) {
            if (this.scheduleFuturesProlog.isEmpty() && this.taskSchedulerProlog == null ) {
                this.taskSchedulerProlog = (ThreadPoolTaskScheduler) applicationContext.getBean("routineThreadPoolTaskScheduler");
            }

            if (this.scheduleFutures.isEmpty() && this.taskScheduler == null ) {
                this.taskScheduler = (ThreadPoolTaskScheduler)applicationContext.getBean("routineThreadPoolTaskScheduler");
            }

            if (this.scheduleFutures2.isEmpty() && this.taskScheduler2 == null) {
                this.taskScheduler2 = (ThreadPoolTaskScheduler)applicationContext.getBean("routineThreadPoolTaskScheduler");
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

                this.scheduledFutureProlog = this.taskSchedulerProlog.schedule(
                        taskProlog,
                        new CronTrigger(cronProlog));

                this.scheduleFuturesProlog.put(device.getId(), this.scheduledFutureProlog);
            }

            if(minute>55){
                hours++;
            }

            int hoursPlusTwo = hours + 2;

            String cronExpression = "* 55 "+hours+"-"+hoursPlusTwo+" * * ?";
//            String cronExpression = "* 0/10 * * * ?";

            System.out.println("CRON EXP : "+cronExpression);

            //task 1

            System.out.println("MASUK THREAD 1");
            task = applicationContext.getBean(ScheduleTaskYoutube.class);
            task.setTask(getScheduleManager(), accountId, objectKey, title,"task1", device, maxqueue);

            this.scheduledFuture = this.taskScheduler.schedule(
                    task,
                    new CronTrigger(cronExpression));

            this.scheduleFutures.put(device.getId(), this.scheduledFuture);


            //task 2

            System.out.println("MASUK THREAD 2");
            int hoursPlusOne = hours + 1;
            hoursPlusTwo = hoursPlusOne + 2;
//            cronExpression = "0/20 * * * * ?";
            cronExpression = "* 55 " + hoursPlusOne + "-" + hoursPlusTwo + " * * ?";
            System.out.println("CRON EXP2 : " + cronExpression);

            task2 = applicationContext.getBean(ScheduleTaskYoutube.class);
            task2.setTask(getScheduleManager(), accountId, objectKey, title, "task2", device, maxqueue);

            this.scheduledFuture2 = this.taskScheduler2.schedule(
                    task2,
                    new CronTrigger(cronExpression));

            this.scheduleFutures2.put(device.getId(), this.scheduledFuture2);

            return true;
        }

        private boolean putScheduleContinue(ESDevice device, long accountId, ObjectNode ObjectKey, String title, String stateTask) {
            if (this.scheduleFutures.isEmpty() && this.taskScheduler == null ) {
                this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("routineThreadPoolTaskScheduler");
            }

            if (this.scheduleFutures2.isEmpty() && this.taskScheduler2 == null) {
                this.taskScheduler2 = (ThreadPoolTaskScheduler) applicationContext.getBean("routineThreadPoolTaskScheduler");
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

            int hoursPlusTwo = 0;

            hoursPlusTwo = hours + 2;

            String cronExpression = "";

            //task 1
            if(stateTask.equalsIgnoreCase("task1")) {
                System.out.println("MASUK THREAD 1 CONTINUE");
                cronExpression = "0 55 "+hours+" * * ?";
                System.out.println("CRON EXP : "+cronExpression);
                task = applicationContext.getBean(ScheduleTaskYoutube.class);
                task.setTask(getScheduleManager(), accountId, ObjectKey, title, "task1",device,maxqueue);

                this.scheduledFuture = this.taskScheduler.schedule(
                        task,
                        new CronTrigger(cronExpression));

                this.scheduleFutures.put(device.getId(), this.scheduledFuture);
            }

            //task 2
            if(stateTask.equalsIgnoreCase("task2")) {

                System.out.println("MASUK THREAD 2 CONTINUE");
                cronExpression = "0 55 " + hours + " * * ?";
                System.out.println("CRON EXP2 : " + cronExpression);

                task2 = applicationContext.getBean(ScheduleTaskYoutube.class);
                task2.setTask(getScheduleManager(), accountId, ObjectKey, title, "task2", device,maxqueue);

                this.scheduledFuture2 = this.taskScheduler2.schedule(
                        task2,
                        new CronTrigger(cronExpression));

                this.scheduleFutures2.put(device.getId(), this.scheduledFuture2);
            }

            return true;
        }

        private boolean removeSchedule(String stateTask, ESDevice device) {

            if(stateTask.equalsIgnoreCase("taskProlog") && this.taskSchedulerProlog != null){
                System.out.println("STOP THREAD PROLOG");
                this.scheduledFutureProlog.cancel(true);
                this.scheduleFuturesProlog.remove(device.getId());

                if (this.scheduleFuturesProlog.isEmpty()) {
                    this.taskSchedulerProlog.destroy();
                    this.taskSchedulerProlog = null;
                }
            }else if(stateTask.equalsIgnoreCase("task1") && this.taskScheduler != null){
                System.out.println("STOP THREAD 1");
                this.scheduledFuture.cancel(true);
                this.scheduleFutures.remove(device.getId());

                if (this.scheduleFutures.isEmpty()) {
                    this.taskScheduler.destroy();
                    this.taskScheduler = null;
                }
            }else if(stateTask.equalsIgnoreCase("task2") && this.taskScheduler2 != null) {
                System.out.println("STOP THREAD 2");
                this.scheduledFuture2.cancel(true);
                this.scheduleFutures2.remove(device.getId());

                if (!this.scheduleFutures2.isEmpty()) {
                    this.taskScheduler2.destroy();
                    this.taskScheduler2 = null;
                }
            }

            return true;
        }

        private boolean removeScheduleByDeviceId(String deviceId, long accountId) {

            System.out.println("REMOVE THREAD SCHEDULE BY DEVICE ID");
            //STOP CURRENT THREAD
            System.out.println("SCHEDULE FUTURE PROLOG : "+this.scheduleFuturesProlog.containsKey(deviceId));

            if (this.scheduleFuturesProlog.containsKey(deviceId)) {
                System.out.println("STOP THREAD PROLOG MANUAL");
                this.scheduledFutureProlog.cancel(true);
                this.scheduleFuturesProlog.clear();
//                this.scheduleFuturesProlog.remove(deviceId);
                this.taskSchedulerProlog.destroy();
                this.taskSchedulerProlog = null;

//                completeYoutubeStream(deviceId,accountId);
            }

            System.out.println("SCHEDULE FUTURE : "+this.scheduleFutures.containsKey(deviceId));
            if (this.scheduleFutures.containsKey(deviceId)) {
                System.out.println("STOP THREAD 1 MANUAL");
                this.scheduledFuture.cancel(true);
                this.scheduleFutures.clear();
//                this.scheduleFutures.remove(deviceId);
                this.taskScheduler.destroy();
                this.taskScheduler = null;

//                completeYoutubeStream(deviceId,accountId);

            }

            System.out.println("SCHEDULE FUTURE 2 : "+this.scheduleFutures2.containsKey(deviceId));

            if (this.scheduleFutures2.containsKey(deviceId)) {
                System.out.println("STOP THREAD 2 MANUAL");
                this.scheduledFuture2.cancel(true);
                this.scheduleFutures2.clear();
//                this.scheduleFutures2.remove(deviceId);
                this.taskScheduler2.destroy();
                this.taskScheduler2 = null;

//                completeYoutubeStream(deviceId,accountId);

            }

            //UPDATE FLAG DELETE ON CAMERA HISTORY
            cameraHistoryRepository.updateDeleteStatus(deviceId);

            return true;
        }

        private boolean updateSchedule(ESDevice device, long accountId,  ObjectNode objectKey, String title, int maxqueue) {
            // Remove existing schedule
            if (this.scheduleFutures.containsKey(device.getId())) {
                ScheduledFuture scheduledFuture = this.scheduleFutures.get(device.getId());
                scheduledFuture.cancel(true);
                this.scheduleFutures.remove(device.getId());
            }

            if (this.scheduleFutures2.containsKey(device.getId())) {
                ScheduledFuture scheduledFuture2 = this.scheduleFutures2.get(device.getId());
                scheduledFuture2.cancel(true);
                this.scheduleFutures2.remove(device.getId());
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
            if(!manualDelete) {
                putScheduleContinue(device, accountId, ObjectKey, title, stateTask);
            }
            return true;
        }

        private int getActiveScheduleCount() {
            return this.taskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
        }

//        private void completeYoutubeStream(String deviceId, long accountId){
//            //GET CURRENT LIVE BROADCAST ID FROM device_tbl
//            ESDevice device = deviceRepository.findByDeviceId(deviceId,accountId);
//            String info = device.getInfo();
//            ObjectNode objectNode = Json.parseToObjectNode(info);
//            System.out.println("broadcastId : "+objectNode);
//            String broadcastId = objectNode.get("youtube_id").toString().replaceAll("[^\\w\\s\\-_]", "");
//            System.out.println("broadcastId : "+broadcastId);
//            //GET ACCESS TOKEN
//            ResponseEntity responseYoutubeKey = accountCameraRepository.YoutubeKey(accountId);
//            ObjectNode objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
//            String accessToken = objectKey.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
//            String clientId = objectKey.get("client_id").toString().replaceAll("[^\\w\\s\\-_.]", "");
//            String clientSecret = objectKey.get("client_secret").toString().replaceAll("[^\\w\\s\\-_.]", "");
//            String refreshToken = objectKey.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");
//
//            //STOP YOUTUBE STREAM
//            String state = "complete";
//            ResponseEntity responseTransitionStop = youtubeService.transitionEvent(accessToken,broadcastId,"streamId",state);
//
//            ObjectNode responseBodyTransitionStop = Json.parseToObjectNode(responseTransitionStop.getBody().toString());
//
//            int statusCode = Integer.parseInt(responseBodyTransitionStop.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
//            System.out.println(statusCode);
//
//            if(responseBodyTransitionStop.get("status_code") != null && statusCode == 401){
//                System.out.println("UNAUTHORIZED");
//                //get access token by Refresh token
//                System.out.println("CLIENT ID : "+clientId);
//                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
//                System.out.println("stop access token : "+accessToken);
//                responseTransitionStop = youtubeService.transitionEvent(accessToken,broadcastId,"streamId",state);
//                responseBodyTransitionStop = Json.parseToObjectNode(responseTransitionStop.getBody().toString());
//
//            }
//
//            //MQTT MESSAGE STOP YOUTUBE
//
//            this.message = MessageBuilder
//                    .withPayload(responseBodyTransitionStop.toString())
//                    .setHeader(MqttHeaders.TOPIC,
//                            "stream/transition/stop")
//                    .build();
//
//            if (applicationEventPublisher != null && message != null) {
//                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
//            } else {
//                System.out.println("MQTT NULL");
//            }
//
//            try{
//                String dataStop = responseBodyTransitionStop.get("data").toString();
//                System.out.println(dataStop);
//            }catch (NullPointerException e){
//                System.out.println(e.getMessage());
//            }
//        }

    }
}