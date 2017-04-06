package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.util.Json;
import io.iotera.util.concurrent.LatchWithResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class ScheduleTask implements Runnable, ApplicationEventPublisherAware {

    @Autowired
    RoutineManager routineManager;

    @Autowired
    ESRoutineRepo routineRepo;

    private long hubId;
    private String routineId;
    private Message<String> message;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initTask(long hubId,
                         String routineId, int routineCategory, String commands, String clients) {
        this.hubId = hubId;
        this.routineId = routineId;

        ObjectNode routineObject = Json.buildObjectNode();
        routineObject.put("rc", routineCategory);
        ObjectNode commandsObject = Json.parseToObjectNode(commands);
        routineObject.set("cms", commandsObject);
        ArrayNode clientsArray = Json.parseToArrayNode(clients);
        routineObject.set("cls", clientsArray);

        if (commandsObject != null) {
            this.message = MessageBuilder
                    .withPayload(Json.toStringIgnoreNull(routineObject))
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topicHub(hubId, CommandPref.SCHEDULE, routineId))
                    .setHeader(MqttHeaders.QOS, 2)
                    .build();
        }
    }

    @Override
    public void run() {

        boolean sent = false;
        int countdown = 6;
        while (countdown >= 0) {

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this,
                        CommandPref.SCHEDULE, this.message));
            } else {
                break;
            }

            try {
                LatchWithResult<Boolean> latch = routineManager.buildLatch(this.routineId);
                latch.getLatch().await(10, TimeUnit.SECONDS);
                routineManager.removeLatch(routineId);
                if (latch.getResult()) {
                    sent = true;
                    break;
                }

            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            --countdown;
        }

        if (sent) {
            routineRepo.updateExecuted(this.routineId, this.hubId);
        }

    }
}
