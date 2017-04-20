package io.iotera.emma.smarthome.camera;

import java.util.Date;

public class CameraRemoveTaskItem {

    private final Date time;
    private final String clientId;
    private final String clietSecret;
    private final String accessToken;
    private final String refreshToken;

    public CameraRemoveTaskItem(Date time, String clientId, String clietSecret, String accessToken, String refreshToken) {
        this.time = time;
        this.clientId = clientId;
        this.clietSecret = clietSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Date getTime() {
        return time;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClietSecret() {
        return clietSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
