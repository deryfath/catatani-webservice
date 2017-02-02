package io.iotera.emma.smarthome.utility;

public class PublishUtility {

    public static String topic(String baseTopic, long accountId, String type) {
        return baseTopic + '/' + accountId + '/' + type;
    }

}
