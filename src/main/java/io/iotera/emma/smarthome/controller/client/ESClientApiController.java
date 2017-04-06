package io.iotera.emma.smarthome.controller.client;

import io.iotera.emma.smarthome.controller.ESApiController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/api")
public class ESClientApiController extends ESApiController {

    @RequestMapping(value = "/mqtt", method = RequestMethod.GET)
    public ResponseEntity read(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long hubId = client.getId();

        return mqtt();
    }

}
