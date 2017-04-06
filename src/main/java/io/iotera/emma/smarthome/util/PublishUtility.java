package io.iotera.emma.smarthome.util;

import io.iotera.util.Text;

public class PublishUtility {

    public static String topic(String baseTopic, long hubId, String type) {
        return baseTopic + '/' + hubId + '/' + type;
    }

    public static String topicHubReg(String suid, String type) {
        return "hubreg/" + suid + '/' + type;
    }

    public static String topicHub(long hubId, String type, String id) {
        String topic = "hub/" + hubId + "/" + type;
        if (!Text.isEmpty(id)) {
            topic += "/" + id;
        }
        return topic;
    }

    public static String topicHub(long hubId, String type) {
        return topicHub(hubId, type, null);
    }

    public static String topicClient(long clientId, String type, String id) {
        String topic = "client/" + clientId + "/" + type;
        if (!Text.isEmpty(id)) {
            topic += "/" + id;
        }
        return topic;
    }

    public static String topicClient(long clientId, String type) {
        return topicClient(clientId, type, null);
    }

    public static String topicClientHub(long hubId, String type, String id) {
        String topic = "client/hub/" + hubId + "/" + type;
        if (!Text.isEmpty(id)) {
            topic += "/" + id;
        }
        return topic;
    }

    public static String topicClientHub(long hubId, String type) {
        return topicClientHub(hubId, type, null);
    }

}
