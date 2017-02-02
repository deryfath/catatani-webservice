package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESDeviceController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hub/device")
public class ESHubDeviceController extends ESDeviceController {

    @Autowired
    PrologVideo prologVideo;

    @Autowired
    YoutubeService youtubeService;

    private String refreshToken = "";
    private String clientId = "";
    private String clientSecret = "";

    private String accessToken = "";

    @Autowired
    ESDeviceRepository deviceRepository;

    @Autowired
    ESAccountCameraRepository accountYoutubeCameraRepository;

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

    @RequestMapping(value = "/create/stream", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createStream(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();
        System.out.println(accountId);
        //get access token, refresh token, client_secret, client_id
        ResponseEntity responseYoutubeKey = accountYoutubeCameraRepository.YoutubeKey(accountId);
        ObjectNode objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));

        accessToken = objectKey.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientId = objectKey.get("client_id").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientSecret = objectKey.get("client_secret").toString().replaceAll("[^\\w\\s\\-_.]", "");
        refreshToken = objectKey.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");

        ObjectNode responseBodyPost = Json.parseToObjectNode(entity.getBody());

        System.out.println(responseBodyPost.get("broadcast_title"));

        String title = responseBodyPost.get("broadcast_title").toString().replaceAll("[^\\w\\s]", "");

        ResponseEntity responseEntity = youtubeService.createEvent(objectKey.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", ""),title);
        ObjectNode responseBody = Json.parseToObjectNode(responseEntity.getBody().toString());

        int statusCode = Integer.parseInt(responseBody.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
        System.out.println(statusCode);

        if(responseBody.get("status_code") != null && statusCode == 401){
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
            responseEntity = youtubeService.createEvent(accessToken,title);

        }

//        scheduleController.create(responseBodyPost,123,accessToken);

        return responseEntity;

    }

    @RequestMapping(value = "/run/ffmpeg", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity runFFFMPEG(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        ObjectNode responseBodyJson = Json.buildObjectNode();

        prologVideo.runVideoProlog("TEST Image",accountId);

        ObjectNode dataObject = Json.buildObjectNode();
        dataObject.put("status", "PROLOG VIDEO RUN");
        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);
    }

    @RequestMapping(value = "/stop/ffmpeg", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity stopFFFMPEG(HttpEntity<String> entity) {

        ObjectNode responseBodyJson = Json.buildObjectNode();
        ObjectNode dataObject = Json.buildObjectNode();

        prologVideo.stopVideoProlog();

        dataObject.put("status", "FFMPEG STOP");
        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);
    }



}




