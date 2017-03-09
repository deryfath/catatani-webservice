package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.repository.ESAccessRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountJpaRepository;
import io.iotera.emma.smarthome.repository.ESAdminRepository.ESAdminJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.util.Json;
import io.iotera.web.spring.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public class ESBaseController extends BaseController {

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESAdminJpaRepository adminJpaRepository;

    @Autowired
    ESAccessRepository accessRepository;

    @Autowired
    Environment env;

    /////////////////////
    // Response Status //
    /////////////////////

    protected ResponseEntity okJsonSuccess(String desc) {
        ObjectNode json = Json.buildObjectNode();
        json.put("status_desc", desc);
        json.put("status_code", 0);
        json.put("status", "success");

        return okJson(json);
    }

    protected ResponseEntity okJsonFailed(int code, String desc) {
        ObjectNode json = Json.buildObjectNode();
        json.put("status_desc", desc);
        json.put("status_code", code);
        json.put("status", "failed");

        return okJson(json);
    }

    protected ResponseEntity okJsonFailedWithErrorMessage(int code, String desc, String message) {
        ObjectNode json = Json.buildObjectNode();
        json.put("status_error", message);
        json.put("status_desc", desc);
        json.put("status_code", code);
        json.put("status", "failed");

        return okJson(json);
    }

    //////////////
    // Property //
    //////////////

    protected String getProperty(String key) {
        return env.getProperty(key);
    }

    ////////////
    // Header //
    ////////////

    protected void authenticateToken(HttpEntity<String> entity) {
        // Get Token Header
        String key = getProperty("header.key.token");
        if (!entity.getHeaders().containsKey(key)) {
            returnBadRequest("header (" + key + ") not found");
        }

        boolean passed = false;
        String token = entity.getHeaders().getFirst(key);
        for (int i = 1; i < 6; ++i) {
            String emmaToken = getProperty("emma.token." + 1);
            if (token.equals(emmaToken)) {
                passed = true;
                break;
            }
        }

        if (!passed) {
            returnUnauthorized("invalid token");
        }
    }

    protected String clientToken(HttpEntity<String> entity) {
        // Get User Token Header
        String key = getProperty("header.key.client.token");
        if (!entity.getHeaders().containsKey(key)) {
            returnUnauthorized("header (" + key + ") not found");
        }
        return entity.getHeaders().getFirst(key);
    }

    protected String accessToken(HttpEntity<String> entity) {
        // Get User Token Header
        String key = getProperty("header.key.access.token");
        if (!entity.getHeaders().containsKey(key)) {
            returnUnauthorized("header (" + key + ") not found");
        }
        return entity.getHeaders().getFirst(key);
    }

    protected String hubToken(HttpEntity<String> entity) {
        // Get User Token Header
        String key = getProperty("header.key.hub.token");
        if (!entity.getHeaders().containsKey(key)) {
            returnUnauthorized("header (" + key + ") not found");
        }
        return entity.getHeaders().getFirst(key);
    }

    protected String adminToken(HttpEntity<String> entity) {
        // Get Admin Token Header
        String key = getProperty("header.key.admin.token");
        if (!entity.getHeaders().containsKey(key)) {
            returnUnauthorized("header (" + key + ") not found");
        }
        return entity.getHeaders().getFirst(key);
    }

    /////////////
    // Account //
    /////////////

    protected ESAccount accountHub(String hubToken) {
        ESAccount account = accountJpaRepository.findByHubTokenAndDeactivateFalse(hubToken);
        if (account == null) {
            returnUnauthorized("invalid hub token");
        }
        return account;
    }

    protected ESAccount accountWithPayment(String hubToken) {
        ESAccount account = accountJpaRepository.findByHubTokenAndDeactivateFalse(hubToken);
        if (account == null) {
            returnUnauthorized("invalid hub token");
        }

        if (!account.isPaymentActive()) {
            returnPaymentRequired("payment required for hub");
        }
        return account;
    }

    protected ESAccount accountAccess(String accessToken, long clientId) {
        ESAccount account = accessRepository.findAccountByAccessToken(accessToken, clientId);
        if (account == null) {
            returnUnauthorized("invalid access token");
        }
        return account;
    }

    ////////////
    // Client //
    ////////////

    protected ESClient client(String clientToken) {
        ESClient client = clientJpaRepository.findByClientTokenAndDeactivateFalse(clientToken);
        if (client == null) {
            returnUnauthorized("invalid client token");
        }
        return client;
    }

    ///////////
    // Admin //
    ///////////

    protected void admin(String adminToken) {
        if (adminJpaRepository.findByToken(adminToken) == null) {
            returnUnauthorized("invalid admin token");
        }
    }

}
