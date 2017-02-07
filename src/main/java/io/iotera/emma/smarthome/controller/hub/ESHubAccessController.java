package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.repository.ESAccessRepository;
import io.iotera.emma.smarthome.repository.ESAccessRepository.ESAccessJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/hub/access")
public class ESHubAccessController extends ESBaseController {

    @Autowired
    ESClientJpaRepository clientJPARepository;

    @Autowired
    ESAccessRepository accessRepository;

    @Autowired
    ESAccessJpaRepository accessJPARepository;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity listAccess(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode accountArray = Json.buildArrayNode();
        List<Object[]> clients = accessRepository.listClientByAccountId(accountId);
        for (Object[] objects : clients) {
            ESClient client = (ESClient) objects[0];
            ESAccess access = (ESAccess) objects[1];

            ObjectNode accountObject = Json.buildObjectNode();

            accountObject.put("id", client.getId());
            accountObject.put("username", client.getUsername());
            accountObject.put("first_name", client.getFirstName());
            accountObject.put("last_name", client.getLastName());
            accountObject.put("picture", client.picturePath(getProperty("host.path")));
            accountObject.put("picture_last_updated", formatDate(
                    client.getPictureLastUpdated()));
            accountObject.put("access_added", formatDate(
                    access.getAddedTime()));
            accountObject.put("account_id", access.getAccountId());

            accountArray.add(accountObject);
        }

        // Result
        response.set("clients", accountArray);
        response.put("status_desc", "");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity addClient(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String clientIdString = get(body, "esclient");
        String username = get(body, "esuser");

        long clientId;
        ESClient client;
        if (clientIdString != null) {
            try {
                clientId = Long.parseLong(clientIdString);
            } catch (NumberFormatException e) {
                return badRequest("wrong client id format");
            }

            client = clientJPARepository.findByIdAndDeactivateFalse(clientId);
            if (client == null) {
                return okJsonFailed(-1, "client_not_found");
            }
        } else if (username != null) {
            client = clientJPARepository.findByUsernameAndDeactivateFalse(username);
            if (client == null) {
                return okJsonFailed(-2, "client_not_found");
            }
            clientId = client.getId();
        } else {
            return badRequest("client required");
        }

        ESAccess access = accessRepository.findByClientIdAccountId(clientId, accountId);
        if (access != null) {
            return okJsonFailed(-3, "access_not_available");
        }
        access = new ESAccess(account.getId(), client.getId());
        accessJPARepository.saveAndFlush(access);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("username", client.getUsername());
        response.put("status_desc", "client_added");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ResponseEntity deleteClient(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String clientIdString = get(body, "esclient");
        long clientId;
        String username = get(body, "esuser");

        ESClient client;
        if (clientIdString != null) {
            clientId = Long.parseLong(clientIdString);
            client = clientJPARepository.findByIdAndDeactivateFalse(clientId);
            if (client == null) {
                return okJsonFailed(-1, "client_not_found");
            }
        } else if (username != null) {
            client = clientJPARepository.findByUsernameAndDeactivateFalse(username);
            if (client == null) {
                return okJsonFailed(-2, "client_not_found");
            }
            clientId = client.getId();
        } else {
            return badRequest("client required");
        }

        ESAccess access = accessRepository.findByClientIdAccountId(clientId, accountId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_found");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("username", client.getUsername());

        access.setDeleted(true);
        access.setDeletedTime(new Date());

        accessJPARepository.saveAndFlush(access);

        response.put("status_desc", "client_deleted");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
