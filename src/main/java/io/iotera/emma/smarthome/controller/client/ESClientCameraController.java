package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESCameraController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/camera")
public class ESClientCameraController extends ESCameraController {

    @RequestMapping(value = "/oauth", method = RequestMethod.POST)
    public ResponseEntity oauth(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = accountAccess(accessToken, clientId);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return oauth(body, hub, hubId);
    }

    @RequestMapping(value = "/oauth/delete", method = RequestMethod.POST)
    public ResponseEntity deleteOauth(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = accountAccess(accessToken, clientId);
        long hubId = hub.getId();

        return deleteOauth(hub, hubId);
    }

    @RequestMapping(value = "/oauth/get", method = RequestMethod.GET)
    public ResponseEntity getOauth(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = accountAccess(accessToken, clientId);
        long hubId = hub.getId();

        return getOauth(hub, hubId);
    }

    @RequestMapping(value = "/history/{cameraId}", method = RequestMethod.GET)
    public ResponseEntity history(
            @PathVariable("cameraId") String cameraId, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = accountAccess(accessToken, clientId);
        long hubId = hub.getId();

        return history(cameraId, hubId);
    }


}
