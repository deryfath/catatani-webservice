package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAuthController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/auth")
public class ESClientAuthController extends ESAuthController {

    @RequestMapping(value = "/login/{method}", method = RequestMethod.POST)
    public ResponseEntity login(
            @PathVariable("method") String method,
            HttpEntity<String> entity) {

        // Request Body
        ObjectNode body = payloadObject(entity);

        return login(body, true, method);
    }

}
