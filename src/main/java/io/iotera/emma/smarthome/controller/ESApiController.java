package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ESApiController extends ESBaseController {

    @Autowired
    ESApplicationInfoRepo.ESApplicationInfoJRepo applicationInfoJRepo;

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity info() {

        ESApplicationInfo info = applicationInfoJRepo.findOne(0L);
        if (info == null) {
            return okJsonSuccess("blank_request_info");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("api_version", info.getApiVersion());
        response.put("hub_name", info.getHubName());
        response.put("hub_package", info.getHubPackage());
        response.put("hub_version", info.getHubVersion());
        response.put("hub_playstore_url", info.getHubPlaystoreUrl());
        response.put("client_name", info.getClientName());
        response.put("client_package", info.getClientPackage());
        response.put("client_version", info.getClientVersion());
        response.put("client_playstore_url", info.getClientPlaystoreUrl());
        response.put("status_desc", "api_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity mqtt() {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("mqtt_url", getProperty("mqtt.url.remote"));
        response.put("mqtt_username", getProperty("mqtt.username"));
        response.put("mqtt_password", getProperty("mqtt.password"));
        response.put("status_desc", "api_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

}
