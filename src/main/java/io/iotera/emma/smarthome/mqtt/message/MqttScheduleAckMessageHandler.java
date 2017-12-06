//package io.iotera.emma.smarthome.mqtt.message;
//
//import io.iotera.emma.smarthome.routine.ScheduleAckEvent;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.context.ApplicationEventPublisherAware;
//import org.springframework.integration.mqtt.support.MqttHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHandler;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.messaging.MessagingException;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.Executors;
//
//@Component("mqttScheduleAckMessageHandler")
//public class MqttScheduleAckMessageHandler implements MessageHandler, ApplicationEventPublisherAware {
//
//    private volatile ApplicationEventPublisher applicationEventPublisher;
//
//    @Override
//    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
//        this.applicationEventPublisher = applicationEventPublisher;
//    }
//
//    @Override
//    public void handleMessage(final Message<?> message) throws MessagingException {
//
//        Executors.newSingleThreadExecutor().submit(new Runnable() {
//            @Override
//            public void run() {
//
//                MessageHeaders messageHeaders = message.getHeaders();
//                String topic = (String) messageHeaders.get(MqttHeaders.TOPIC);
//
//                String[] token = topic.split("/");
//                if (token.length < 5) {
//                    return;
//                }
//
//                long hubId = -1;
//                try {
//                    hubId = Long.parseLong(token[2]);
//                } catch (NumberFormatException e) {
//                    //e.printStackTrace();
//                    return;
//                }
//                String routineId = token[4];
//
//                if (applicationEventPublisher != null) {
//                    applicationEventPublisher.publishEvent(new ScheduleAckEvent(
//                            MqttScheduleAckMessageHandler.this, routineId));
//                }
//            }
//        });
//
//    }
//
//}
