package io.iotera.emma.smarthome.camera;

import io.iotera.emma.smarthome.youtube.YoutubeItem;

public class CameraStartTaskItem {

    private String clientId;
    private String clietSecret;
    private String accessToken;
    private String refreshToken;
    private int maxQueue;
    private YoutubeItem youtubeItem;

    public CameraStartTaskItem(String clientId, String clietSecret, String accessToken, String refreshToken,
                               int maxQueue, YoutubeItem youtubeItem) {
        this.clientId = clientId;
        this.clietSecret = clietSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.maxQueue = maxQueue;
        this.youtubeItem = youtubeItem;
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

    public int getMaxQueue() {
        return maxQueue;
    }

    public YoutubeItem getYoutubeItem() {
        return youtubeItem;
    }

}
