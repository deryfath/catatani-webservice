package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESHubController;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.util.Encrypt;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/auth")
public class ESHubAuthController extends ESHubController {

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity check(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String key = getProperty("header.key.hub.hmac");
        if (!entity.getHeaders().containsKey(key)) {
            return unauthorized("header (" + key + ") not found");
        }
        String hmac = entity.getHeaders().getFirst(key);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String uid = rget(body, "esuid");
        String suid = rget(body, "essuid");
        long timestamp = rget(body, "estime", Long.class);

        if (!hmac.equals(Encrypt.MD5("Emma-" + suid + "-" + timestamp))) {
            return unauthorized("Invalid Hmac");
        }

        return check(uid, suid);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        return login(hub);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity logout(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Hub
        ESHub hub = accountHub(hubToken);
        long hubId = hub.getId();

        return logout(hub);
    }

}


