package io.iotera.emma.smarthome.youtube;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.util.Json;

import java.text.SimpleDateFormat;
import java.util.Date;

public class YoutubeItem {

    private final String broadcastId;
    private final String streamId;
    private final String streamKey;
    private final String url;
    private Date time;

    public YoutubeItem(String broadcastId, String streamId, String streamKey, String url) {
        this.broadcastId = broadcastId;
        this.streamId = streamId;
        this.streamKey = streamKey;
        this.url = url;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String getUrl() {
        return url;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public ObjectNode getInfo() {
        ObjectNode node = Json.buildObjectNode();
        node.put("ybid", broadcastId);
        node.put("ysid", streamId);
        node.put("ysk", streamKey);
        node.put("yurl", url);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        node.put("tm", sdf.format(time));
        return node;
    }

    public ObjectNode getInfoOld() {
        ObjectNode node = Json.buildObjectNode();
        node.put("yobid", broadcastId);
        node.put("yosid", streamId);
        node.put("yosk", streamKey);
        node.put("yourl", url);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        node.put("otm", sdf.format(time));
        return node;
    }

}
