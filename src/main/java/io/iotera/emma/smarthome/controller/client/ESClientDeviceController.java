package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESDeviceController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/device")
public class ESClientDeviceController extends ESDeviceController {

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

    @RequestMapping(value = "/listcategory/{category}", method = RequestMethod.GET)
    public ResponseEntity listRoom(
            @PathVariable("category") int category, HttpEntity<String> entity) {

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
        return listCategory(category, accountId);
    }

    @RequestMapping(value = "/listroom/{roomId}", method = RequestMethod.GET)
    public ResponseEntity listRoom(
            @PathVariable("roomId") String roomId, HttpEntity<String> entity) {

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
        return listRoom(roomId, accountId);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity create(HttpEntity<String> entity) {

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
        ESAccount account = adminAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result
        return create(body, accountId);
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public ResponseEntity read(
            @PathVariable("id") String deviceId, HttpEntity<String> entity) {

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
        return read(deviceId, accountId);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

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
        ESAccount account = adminAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result
        return update(body, accountId);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ResponseEntity delete(HttpEntity<String> entity) {

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
        ESAccount account = adminAccess(accessToken, clientId);
        long accountId = account.getId();

        // Result
        return delete(body, accountId);
    }

}
