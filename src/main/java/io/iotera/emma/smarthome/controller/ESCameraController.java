package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.model.account.ESHubCamera;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepo;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.emma.smarthome.repository.ESHubCameraRepo;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ESCameraController extends ESBaseController {

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESHubCameraRepo.ESHubCameraJRepo hubCameraJRepo;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    protected ResponseEntity oauth(ObjectNode body, ESHub hub, long hubId) {

        // Request Body
        String oauthCode = rget(body, "esoauth");
        String youtubeId = rget(body, "esyid");
        String youtubeEmail = rget(body, "esyemail");

        if (!hubCameraRepo.isYoutubeIdAvailable(youtubeId, hubId)) {
            return okJsonFailed(-1, "youtube_id_not_available");
        }
        Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
        if (youtubeClientApi == null) {
            return internalServerError("internal_server_error");
        }
        String clientId = youtubeClientApi._1;
        String clientSecret = youtubeClientApi._2;
        String accessToken = null;
        String refreshToken = null;

        Tuple.T3<Integer, String, String> result =
                youtubeService.retrieveAccessTokenAndRefreshToken(oauthCode, clientId, clientSecret);
        for (int i = 0; i < 5; ++i) {
            if (result._1 == 200) {
                accessToken = result._2;
                refreshToken = result._3;
                break;
            }
        }

        if (accessToken == null || refreshToken == null) {
            return okJsonFailed(-2, "failed_to_generate_youtube_api_token");
        }

        int yStatusCode = 403;
        for (int i = 0; i < 5; ++i) {
            Tuple.T2<Integer, ObjectNode> result2 = youtubeService.retrieveListEvent(accessToken);
            if (result2._1 == 200) {
                yStatusCode = 200;
                break;
            }
        }

        if (yStatusCode == 403) {
            return okJsonFailed(-3, "youtube_stream_is_not_activated");
        }

        ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
        if (hubCamera == null) {
            hubCamera = new ESHubCamera(accessToken, refreshToken, youtubeId,
                    youtubeEmail, 24, hub, hubId);
        } else {
            hubCamera.setAccessToken(accessToken);
            hubCamera.setRefreshToken(refreshToken);
        }
        hubCameraJRepo.saveAndFlush(hubCamera);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", hubId);
        response.put("youtube_id", hubCamera.getYoutubeId());
        response.put("youtube_email", hubCamera.getYoutubeEmail());
        response.put("max_history", hubCamera.getMaxHistory());
        response.put("status_desc", "success");
        response.put("status_code", 0);

        return okJson(response);
    }

    protected ResponseEntity getOauth(ESHub hub, long hubId) {

        ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
        if (hubCamera == null) {
            return okJsonFailed(-1, "hub_camera_not_found");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", hubId);
        response.put("youtube_id", hubCamera.getYoutubeId());
        response.put("youtube_email", hubCamera.getYoutubeEmail());
        response.put("max_history", hubCamera.getMaxHistory());
        response.put("status_desc", "success");
        response.put("status_code", 0);

        return okJson(response);
    }

    protected ResponseEntity deleteOauth(ESHub hub, long hubId) {

        ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
        if (hubCamera == null) {
            return okJsonFailed(-1, "hub_camera_not_found");
        }
        hubCameraJRepo.delete(hubCamera);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("status_desc", "success");
        response.put("status_code", 0);

        return okJson(response);
    }

    protected ResponseEntity history(String cameraId, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode cameraArray = Json.buildArrayNode();
        List<ESCameraHistory> cameraHistories =
                cameraHistoryRepo.listShownHistoryByCameraId(cameraId, hubId);
        for (ESCameraHistory cameraHistory : cameraHistories) {
            ObjectNode cameraObject = Json.buildObjectNode();

            cameraObject.put("id", cameraHistory.getId());
            cameraObject.put("youtube_title", cameraHistory.getYoutubeTitle());
            cameraObject.put("youtube_url", cameraHistory.getYoutubeUrl());
            cameraObject.put("youtube_broadcast_id", cameraHistory.getYoutubeBroadcastId());
            cameraObject.put("youtube_stream_id", cameraHistory.getYoutubeStreamId());
            cameraObject.put("youtube_stream_key", cameraHistory.getYoutubeStreamKey());
            cameraObject.put("history_time", formatDate(cameraHistory.getHistoryTime()));
            cameraObject.put("parent", cameraHistory.getParent());
            cameraObject.put("camera_id", cameraId);

            cameraArray.add(cameraObject);
        }

        response.set("cameras", cameraArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
