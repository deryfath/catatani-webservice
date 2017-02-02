package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountLocation;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.repository.ESAccessRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/access")
public class ESClientAccessController extends ESBaseController {

    @Autowired
    ESAccountRepository.ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccessRepository accessRepository;

    @Autowired
    ESAccessRepository.ESAccessJpaRepository accessJPARepository;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity listAccess(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode accountArray = Json.buildArrayNode();
        List<Object[]> accounts = accessRepository.listAccountByClientId(clientId);
        for (Object[] objects : accounts) {
            ESAccount account = (ESAccount) objects[0];
            ESAccess access = (ESAccess) objects[1];

            ObjectNode accountObject = Json.buildObjectNode();

            ESAccountLocation location = account.getAccountLocation();
            accountObject.put("id", account.getId());
            accountObject.put("name", account.getName());
            accountObject.put("picture", account.picturePath(getProperty("host.path")));
            accountObject.put("picture_last_updated", formatDate(
                    account.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
            accountObject.put("latitude", location.getLatitude());
            accountObject.put("longitude", location.getLongitude());
            accountObject.put("registered_date", formatDate(
                    account.getRegisteredDate(), "yyyy-MM-dd HH:mm:ss"));
            accountObject.put("atoken", access.getAccessToken());
            accountObject.put("client_id", clientId);

            accountArray.add(accountObject);
        }

        // Result
        response.set("accounts", accountArray);
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public ResponseEntity getAccess(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String accountIdString = rget(body, "esaccount");
        long accountId;
        try {
            accountId = Long.parseLong(accountIdString);
        } catch (NumberFormatException e) {
            return badRequest("wrong client id format");
        }

        ESAccess access = accessRepository.findByClientIdAccountId(clientId, accountId);
        if (access == null) {
            return okJsonFailed(-1, "");
        }

        ObjectNode response = Json.buildObjectNode();
        response.put("atoken", access.getAccessToken());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    public ResponseEntity connect(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String accountIdString = rget(body, "esaccount");
        long accountId;
        try {
            accountId = Long.parseLong(accountIdString);
        } catch (NumberFormatException e) {
            return badRequest("wrong account id format");
        }

        ESAccount account = accountJpaRepository.findByIdAndDeactivateFalse(accountId);
        if (account == null) {
            return okJsonFailed(-1, "account_not_found");
        }

        ESAccess access = accessRepository.findByClientIdAccountId(clientId, accountId);
        if (access == null) {
            return okJsonFailed(-2, "access_not_available");
        }

        access = new ESAccess(account.getId(), client.getId());
        accessJPARepository.saveAndFlush(access);

        // Result
        return okJsonSuccess("connect_success");
    }

}
