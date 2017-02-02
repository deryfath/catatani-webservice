package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;

@Entity
@Table(name = "account_camera_tbl")
public class ESAccountCamera {

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

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    public ESAccountCamera() {
    }

    public ESAccountCamera(long id, String accessToken, String refreshToken, String youtubeId, String youtubeEmail,
                           int maxHistory, ESAccount account) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.youtubeId = youtubeId;
        this.youtubeEmail = youtubeEmail;
        this.maxHistory = maxHistory;
        this.account = account;
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

    public ESAccount getAccount() {
        return account;
    }

    public void setAccount(ESAccount account) {
        this.account = account;
    }
}
