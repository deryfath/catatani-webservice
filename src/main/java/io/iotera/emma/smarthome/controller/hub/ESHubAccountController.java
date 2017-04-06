package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESHubController;
import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/account")
public class ESHubAccountController extends ESHubController {

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity read(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        return read(hub);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return update(body, hub, hubId);
    }

}
