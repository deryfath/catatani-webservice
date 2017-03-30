package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESDeviceController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.repository.ESDeviceRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/device")
public class ESHubDeviceController extends ESDeviceController {

    @Autowired
    ESDeviceRepo deviceRepository;

    @RequestMapping(value = "/listall", method = RequestMethod.GET)
    public ResponseEntity listAll(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return listAll(accountId);
    }

    @RequestMapping(value = "/listcategory/{category}", method = RequestMethod.GET)
    public ResponseEntity listRoom(
            @PathVariable("category") int category, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return listCategory(category, accountId);
    }

    @RequestMapping(value = "/listroom/{roomId}", method = RequestMethod.GET)
    public ResponseEntity listRoom(
            @PathVariable("roomId") String roomId, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return listRoom(roomId, accountId);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity create(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return create(body, accountId);
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public ResponseEntity read(
            @PathVariable("id") String deviceId, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return read(deviceId, accountId);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return update(body, accountId);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ResponseEntity delete(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return delete(body, accountId);
    }

    @RequestMapping(value = "/edit/address", method = RequestMethod.POST)
    public ResponseEntity updateAddress(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        ArrayNode devices = rget(body, "esdevices", ArrayNode.class);

        // Response
        ObjectNode response = Json.buildObjectNode();
        ArrayNode nodes = Json.buildArrayNode();
        int updatedCount = 0;

        for (JsonNode node : devices) {
            if (!node.isObject()) {
                return badRequest("node is not json object");
            }

            ObjectNode device = (ObjectNode) node;
            String uid = rget(device, "esuid");
            String address = rget(device, "esaddress");

            int updated = deviceRepository.updateAddress(address, uid, accountId) > 0 ? 1 : 0;
            updatedCount += updated;
            if (updated > 0) {
                nodes.add(uid);
            }
        }

        // Result
        if (nodes.size() == 0) {
            response.put("device_updated_count", 0);
            response.set("device_updated", nodes);
            response.put("status_desc", "no_device_address_updated");
            response.put("status_code", 101);
            response.put("status", "success");
            return okJson(response);
        }

        response.put("device_updated_count", updatedCount);
        response.set("device_updated", nodes);
        response.put("status_desc", "devices_address_updated");
        response.put("status_code", 0);
        response.put("status", "success");
        return okJson(response);
    }

}
