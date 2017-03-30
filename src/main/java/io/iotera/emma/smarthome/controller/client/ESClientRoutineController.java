package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESRoutineController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/routine")
public class ESClientRoutineController extends ESRoutineController {

    @RequestMapping(value = "/listall", method = RequestMethod.GET)
    public ResponseEntity listAll(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = accountAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result
        return listAll(accountId);
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public ResponseEntity activate(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = accountAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result

        return activate(body, accountId);
    }

}
