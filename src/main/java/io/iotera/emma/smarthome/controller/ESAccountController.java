package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public class ESAccountController extends ESBaseController {


    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountParuruJpaRepository accountParuruJpaRepository;

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientParuruJpaRepository clientParuruJpaRepository;


    public ResponseEntity updateHubPassword(ObjectNode body, ESAccount account) {

        String password = rget(body, "espass");
        String newPassword = rget(body, "esnpass");

        if (password.equals(newPassword)) {
            return okJsonFailed(-2, "password_and_new_password_same");
        }

        ESAccountParuru accountParuru = account.getAccountParuru();

        String savedpass = account.getPassword();
        String paruru = accountParuru.getParuru();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
        String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);
        if (!savedpass.equals(hashedpass)) {
            return okJsonFailed(-1, "invalid_password");
        }

        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        accountParuru.setParuru(newParuru);

        accountJpaRepository.saveAndFlush(account);
        accountParuruJpaRepository.saveAndFlush(accountParuru);

        return okJsonSuccess("change_password_success");
    }

    public ResponseEntity updateClientPassword(ObjectNode body, ESClient client) {

        // Request Body
        String password = rget(body, "espass");
        String newPassword = rget(body, "esnpass");

        if (password.equals(newPassword)) {
            return okJsonFailed(-2, "password_and_new_password_same");
        }

        ESClientParuru clientParuru = client.getClientParuru();

        String savedpass = client.getPassword();
        String paruru = clientParuru.getParuru();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
        String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);
        if (!savedpass.equals(hashedpass)) {
            return okJsonFailed(-1, "invalid_password");
        }

        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        client.setPassword(newHashedpass);
        clientParuru.setParuru(newParuru);

        clientJpaRepository.saveAndFlush(client);
        clientParuruJpaRepository.saveAndFlush(clientParuru);

        // Response
        return okJsonSuccess("change_password_success");
    }

}
