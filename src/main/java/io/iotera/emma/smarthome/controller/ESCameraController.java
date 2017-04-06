package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.repository.ESCameraHistoryRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ESCameraController extends ESBaseController {

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepository;

    protected ResponseEntity listHistoryByDeviceId(String deviceId, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode cameraArray = Json.buildArrayNode();
        List<ESCameraHistory> cameraHistories =
                cameraHistoryRepository.listCameraHistoryByDeviceId(deviceId, hubId);
        for (ESCameraHistory cameraHistory : cameraHistories) {
            ObjectNode cameraObject = Json.buildObjectNode();

            cameraObject.put("id", cameraHistory.getId());
            cameraObject.put("youtube_title", cameraHistory.getYoutubeTitle());
            cameraObject.put("youtube_url", cameraHistory.getYoutubeUrl());
            cameraObject.put("youtube_broadcast_id", cameraHistory.getYoutubeBroadcastId());
            cameraObject.put("youtube_stream_id", cameraHistory.getYoutubeStreamId());
            cameraObject.put("youtube_stream_key", cameraHistory.getYoutubeStreamKey());
            cameraObject.put("history_time", formatDate(cameraHistory.getHistoryTime()));
            cameraObject.put("device_id", deviceId);

            cameraArray.add(cameraObject);
        }

        response.set("cameras", cameraArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
