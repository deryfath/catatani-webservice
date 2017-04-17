package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAccountController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/account")
public class ESClientAccountController extends ESAccountController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity read(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        return read(client);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity updateClient(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Hub
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return update(body, client, clientId);
    }

    @RequestMapping(value = "/edit/password", method = RequestMethod.POST)
    public ResponseEntity updatePassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Hub
        ESAccount client = accountClient(clientToken);
        long hubId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        return updatePassword(body, client);
    }

    @RequestMapping(value = "/camera/youtube/oauth", method = RequestMethod.POST)
    public ResponseEntity retrieveYoutubeKeyInitiate(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = adminAccess(accessToken, clientId);
        long hubId = hub.getId();

        // Response
        return getYoutubeKey(body,hub);
    }


}
