package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESCameraController;
import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/camera")
public class ESHubCameraController extends ESCameraController {

    @RequestMapping(value = "/oauth", method = RequestMethod.POST)
    public ResponseEntity oauth(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return oauth(body, hub, hubId);
    }

    @RequestMapping(value = "/history/{cameraId}", method = RequestMethod.GET)
    public ResponseEntity history(
            @PathVariable("cameraId") String cameraId, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        return history(cameraId, hubId);
    }

}
