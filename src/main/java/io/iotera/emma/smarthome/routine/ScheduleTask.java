package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESRoutineRepository;
import io.iotera.emma.smarthome.utility.PublishUtility;
import io.iotera.util.concurrent.LatchWithResult;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
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
    Environment env;

    @Autowired
    ESRoutineRepository routineRepository;

    private long accountId;
    private String routineId;
    private Message<String> message;

    private volatile ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setTask(long accountId,
                        String routineId, int routineCategory, String commands, String clients) {
        this.accountId = accountId;
        this.routineId = routineId;

        ObjectNode routineObject = Json.buildObjectNode();
        routineObject.put("rid", routineId);
        routineObject.put("rc", routineCategory);
        ObjectNode commandsObject = Json.parseToObjectNode(commands);
        routineObject.set("cm", commandsObject);
        ArrayNode clientsArray = Json.parseToArrayNode(clients);
        routineObject.set("cl", clientsArray);

        if (commandsObject != null) {
            this.message = MessageBuilder
                    .withPayload(Json.toStringIgnoreNull(routineObject))
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topic(env.getProperty("mqtt.topic.command"),
                                    accountId,
                                    CommandPref.ROUTINE))
                    .build();
        }
    }

    @Override
    public void run() {

        boolean sent = false;
        int countdown = 6;
        while (countdown >= 0) {

            if (applicationEventPublisher != null && message != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, this.message));
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
            routineRepository.updateExecuted(this.routineId, this.accountId);
        }

    }
}
