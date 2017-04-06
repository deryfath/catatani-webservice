package io.iotera.emma.smarthome.model.device;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESCameraHistory.NAME)
public class ESCameraHistory {

    public static final String NAME = "v2_camera_history_tbl";

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(name = "youtube_title")
    protected String youtubeTitle;

    @Column(name = "youtube_url")
    protected String youtubeUrl;

    @Column(name = "youtube_broadcast_id")
    protected String youtubeBroadcastId;

    @Column(name = "youtube_stream_id")
    protected String youtubeStreamId;

    @Column(name = "youtube_stream_key")
    protected String youtubeStreamKey;

    @Column(name = "history_time")
    protected Date historyTime;

    @Column(name = "__order__", nullable = false)
    protected long order;

    //////////////////
    // Deleted Flag //
    @Column(name = "__deleted_flag__", nullable = false)
    protected boolean deleted;

    @Column(name = "__deleted_time__")
    protected Date deletedTime;

    ////////////
    // Column //
    ////////////

    @Column(name = "__parent__", nullable = false)
    protected String parent;

    /////////////////
    // Constructor //
    /////////////////

    public ESCameraHistory() {
    }

    public ESCameraHistory(String youtubeTitle, String youtubeUrl, String youtubeBroadcastId, String youtubeStreamId,
                           String youtubeStreamKey, Date historyTime, String parent) {
        this.youtubeTitle = youtubeTitle;
        this.youtubeUrl = youtubeUrl;
        this.youtubeBroadcastId = youtubeBroadcastId;
        this.youtubeStreamId = youtubeStreamId;
        this.youtubeStreamKey = youtubeStreamKey;

        this.historyTime = historyTime;
        this.order = 0;
        this.deleted = false;

        this.parent = parent;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public static String parent(String deviceId, String roomId, long hubId) {
        StringBuilder pBuilder = new StringBuilder();
        pBuilder.append(hubId);
        pBuilder.append('/');
        roomId = (roomId != null) ? roomId : "%";
        pBuilder.append(roomId);
        if (roomId.equals("%")) {
            if (deviceId != null) {
                pBuilder.append('/');
            }
        } else {
            pBuilder.append('/');
        }
        if (deviceId != null) {
            pBuilder.append(deviceId);
            if (!deviceId.equals("%")) {
                pBuilder.append('/');
            }
        }

        return pBuilder.toString();
    }

    public String getId() {
        return id;
    }

    public String getYoutubeTitle() {
        return youtubeTitle;
    }

    public void setYoutubeTitle(String youtubeTitle) {
        this.youtubeTitle = youtubeTitle;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getYoutubeBroadcastId() {
        return youtubeBroadcastId;
    }

    public void setYoutubeBroadcastId(String youtubeBroadcastId) {
        this.youtubeBroadcastId = youtubeBroadcastId;
    }

    public String getYoutubeStreamId() {
        return youtubeStreamId;
    }

    public void setYoutubeStreamId(String youtubeStreamId) {
        this.youtubeStreamId = youtubeStreamId;
    }

    public String getYoutubeStreamKey() {
        return youtubeStreamKey;
    }

    public void setYoutubeStreamKey(String youtubeStreamKey) {
        this.youtubeStreamKey = youtubeStreamKey;
    }

    public Date getHistoryTime() {
        return historyTime;
    }

    public void setHistoryTime(Date historyTime) {
        this.historyTime = historyTime;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
