package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAccessController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/access")
public class ESClientAccessController extends ESAccessController {

    @RequestMapping(value = "/home/connect", method = RequestMethod.POST)
    public ResponseEntity connect(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return connect(body, client, clientId);
    }

    @RequestMapping(value = "/home/list", method = RequestMethod.GET)
    public ResponseEntity listHome(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        return listHome(client, clientId);
    }

    @RequestMapping(value = "/home/get", method = RequestMethod.GET)
    protected ResponseEntity getAccess(HttpEntity<String> entity) {

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

        return getAccess(client, clientId, hub, hubId);
    }

    @RequestMapping(value = "/home/member/list", method = RequestMethod.GET)
    public ResponseEntity listMember(HttpEntity<String> entity) {

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

        return listMember(hub, hubId);
    }

    @RequestMapping(value = "/home/member/add", method = RequestMethod.POST)
    public ResponseEntity createMember(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = adminAccess(accessToken, clientId);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return createMember(body, hub, hubId);
    }

    @RequestMapping(value = "/home/member/admin", method = RequestMethod.POST)
    public ResponseEntity updateAdmin(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = adminAccess(accessToken, clientId);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return updateAdmin(body, hub, hubId);
    }

    @RequestMapping(value = "/home/member/remove", method = RequestMethod.POST)
    public ResponseEntity deleteMember(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Hub
        ESHub hub = adminAccess(accessToken, clientId);
        long hubId = hub.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return deleteMember(body, hub, hubId);
    }


}
