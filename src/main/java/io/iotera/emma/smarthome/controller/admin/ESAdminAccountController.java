package io.iotera.emma.smarthome.controller.admin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.client.ESClientParuru;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountParuruJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientParuruJpaRepository;
import io.iotera.emma.smarthome.utility.ESUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/account")
public class ESAdminAccountController extends ESBaseController {

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountParuruJpaRepository accountParuruJpaRepository;

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientParuruJpaRepository clientParuruJpaRepository;

    @RequestMapping(value = "/edit/hub/password", method = RequestMethod.POST)
    public ResponseEntity updateHubPassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String adminToken = adminToken(entity);
        admin(adminToken);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Account
        ESAccount account = accountJpaRepository.findByIdAndDeactivateFalse(rget(body, "esaccount", Long.class));
        long accountId = account.getId();

        String newPassword = rget(body, "esnpass");
        ESAccountParuru accountParuru = account.getAccountParuru();

        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        account.generateHubToken();
        accountParuru.setParuru(newParuru);

        accountJpaRepository.saveAndFlush(account);
        accountParuruJpaRepository.saveAndFlush(accountParuru);

        return okJsonSuccess("change_password_success");
    }

    @RequestMapping(value = "/edit/hub/client", method = RequestMethod.POST)
    public ResponseEntity updateClientPassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String adminToken = adminToken(entity);
        admin(adminToken);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESClient client = clientJpaRepository.findByIdAndDeactivateFalse(rget(body, "esclient", Long.class));
        long clientId = client.getId();

        String newPassword = rget(body, "esnpass");
        ESClientParuru clientParuru = client.getClientParuru();

        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        client.setPassword(newHashedpass);
        client.generateClientToken();
        clientParuru.setParuru(newParuru);

        clientJpaRepository.saveAndFlush(client);
        clientParuruJpaRepository.saveAndFlush(clientParuru);

        // Response
        return okJsonSuccess("change_password_success");
    }

}
