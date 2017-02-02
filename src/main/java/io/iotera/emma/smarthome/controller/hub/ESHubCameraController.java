package io.iotera.emma.smarthome.controller.hub;

import io.iotera.emma.smarthome.controller.ESCameraController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hub/device/camera")
public class ESHubCameraController extends ESCameraController {

    @RequestMapping(value = "/listhistory/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity listHistoryByDeviceId(
            @PathVariable("deviceId") String deviceId, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return listHistoryByDeviceId(deviceId);
    }

}
