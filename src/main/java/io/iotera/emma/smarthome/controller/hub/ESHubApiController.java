package io.iotera.emma.smarthome.controller.hub;

import io.iotera.emma.smarthome.controller.ESApiController;
import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/api")
public class ESHubApiController extends ESApiController {

    @RequestMapping(value = "/mqtt", method = RequestMethod.GET)
    public ResponseEntity read(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        return mqtt();
    }

}
