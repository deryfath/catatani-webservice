package io.iotera.emma.smarthome.util;

import io.iotera.util.Text;

public class PublishUtility {

    public static String topic(String baseTopic, long accountId, String type) {
        return baseTopic + '/' + accountId + '/' + type;
    }

    public static String topicHub(long accountId, String type, String id) {
        String topic = "hub/" + accountId + "/" + type;
        if (!Text.isEmpty(id)) {
            topic += "/" + id;
        }
        return topic;
    }

    public static String topicHub(long accountId, String type) {
        return topicHub(accountId, type, null);
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

    public static String topicClientHub(long accountId, String type, String id) {
        String topic = "client/hub/" + accountId + "/" + type;
        if (!Text.isEmpty(id)) {
            topic += "/" + id;
        }
        return topic;
    }

    public static String topicClientHub(long accountId, String type) {
        return topicClientHub(accountId, type, null);
    }

}
