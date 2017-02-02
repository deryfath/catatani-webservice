package io.iotera.emma.smarthome.controller.client;

import io.iotera.emma.smarthome.controller.ESRoutineController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.client.ESClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/routine")
public class ESClientRoutineController extends ESRoutineController {

    @RequestMapping(value = "/listall", method = RequestMethod.GET)
    public ResponseEntity listAll(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = accountAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result
        return listAll(accountId);
    }

}
