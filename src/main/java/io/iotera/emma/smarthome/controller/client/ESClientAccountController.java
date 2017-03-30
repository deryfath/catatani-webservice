package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Base64;
import io.iotera.emma.smarthome.controller.ESAccountController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountHub;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Json;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/client/account")
public class ESClientAccountController extends ESAccountController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountHubJRepo accountHubJRepo;

    @RequestMapping(value = "/get/{attr}", method = RequestMethod.GET)
    public ResponseEntity read(@PathVariable String[] attr, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        List<String> attrList = Arrays.asList(attr);

        return read(attrList, client);
    }


    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity updateClient(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Account
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return update(body, true, client, clientId);
    }

    @RequestMapping(value = "/hub/edit", method = RequestMethod.POST)
    public ResponseEntity updateHub(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);
        String accessToken = accessToken(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Account
        ESAccount account = adminAccess(accessToken, clientId);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return update(body, false, account, accountId);
    }

    @RequestMapping(value = "/hub/activate", method = RequestMethod.POST)
    public ResponseEntity activate(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESAccount client = accountClient(clientToken);
        long clientId = client.getId();

        // Response
        ObjectNode response = Json.buildObjectNode();

        if (!client.isHubActive()) {
            if (has(body, "esname") || has(body, "espic")) {
                ESAccountHub accountHub = client.getAccountHub();
                boolean editHub = false;

                if (has(body, "esname")) {
                    String name = get(body, "esname");
                    if (!accountHub.getName().equals(name)) {
                        accountHub.setName(name);
                        editHub = true;
                    }
                    response.put("name", name);
                }

                if (has(body, "espic")) {
                    String picture = get(body, "espic");
                    String path = ResourceUtility.hubPath(clientId, "hub");
                    String attachment = getProperty("attachment.path");

                    // Delete current picture
                    if (accountHub.getPicture() != null) {
                        String filename = ResourceUtility.filename(accountHub.getPicture());
                        ResourceUtility.delete(attachment, path, filename);
                    }
                    if (!picture.isEmpty()) {
                        // Update
                        byte[] data = Base64.decodeBase64(picture);
                        String newFilename = Random.alphaNumericLowerCase(8);
                        ResourceUtility.save(data, attachment, path, newFilename);
                        accountHub.setPicture(path + "/" + newFilename);
                    } else {
                        // Delete
                        accountHub.setPicture(null);
                    }
                    accountHub.setPictureLastUpdated(new Date());
                    editHub = true;

                    response.put("picture", accountHub.picturePath(getProperty("host.path.remote")));
                    response.put("picture_last_updated", formatDate(accountHub.getPictureLastUpdated()));
                }

                if (editHub) {
                    accountHubJRepo.saveAndFlush(accountHub);
                }
            }

            client.generateHubToken();
            client.setHubActive(true);
            client.setHubActiveDate(new Date());
            accountJRepo.saveAndFlush(client);
        }
        response.put("status_desc", "activate_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/edit/password", method = RequestMethod.POST)
    public ResponseEntity updatePassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Account
        ESAccount client = accountClient(clientToken);
        long accountId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        return updatePassword(body, client);
    }


}
