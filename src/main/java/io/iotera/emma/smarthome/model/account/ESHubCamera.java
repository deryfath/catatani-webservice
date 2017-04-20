package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;

@Entity
@Table(name = ESHubCamera.NAME)
public class ESHubCamera {

    public static final String NAME = "v2_hub_camera_tbl";

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "access_token")
    protected String accessToken;

    @Column(name = "refresh_token")
    protected String refreshToken;

    @Column(name = "youtube_id")
    protected String youtubeId;

    @Column(name = "youtube_email")
    protected String youtubeEmail;

    @Column(name = "max_history")
    protected int maxHistory;

    ////////////
    // Column //
    ////////////

    @OneToOne
    @JoinColumn(name = "hub_id")
    protected ESHub hub;

    /////////////////
    // Constructor //
    /////////////////

    protected ESHubCamera() {
    }

    public ESHubCamera(String accessToken, String refreshToken, String youtubeId, String youtubeEmail,
                       int maxHistory, ESHub hub, long hubId) {
        this.id = hubId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.youtubeId = youtubeId;
        this.youtubeEmail = youtubeEmail;
        this.maxHistory = maxHistory;
        this.hub = hub;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getYoutubeEmail() {
        return youtubeEmail;
    }

    public void setYoutubeEmail(String youtubeEmail) {
        this.youtubeEmail = youtubeEmail;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public ESHub getHub() {
        return hub;
    }

}
