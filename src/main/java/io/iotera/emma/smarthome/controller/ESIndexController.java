package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository.ESApplicationInfoJpaRepository;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ESIndexController extends ESBaseController {

    @Autowired
    ESApplicationInfoJpaRepository applicationInfoJpaRepository;

    @Autowired
    ESApplicationInfoRepository applicationInfoRepository;

    @RequestMapping(value = "api/info", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity info() {

        ESApplicationInfo info = applicationInfoJpaRepository.findOne(0L);
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
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "test/lmao", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity lmao() {

        //System.out.println(applicationInfoRepository.getClientIdAndClientSecret());

        return ResponseEntity.ok("");
    }

}
