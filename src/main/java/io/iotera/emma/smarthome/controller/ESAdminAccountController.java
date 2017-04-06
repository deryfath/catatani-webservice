package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.PasswordUtility;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;

@RestController
@RequestMapping("/admin/account")
public class ESAdminAccountController extends ESBaseController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountParuruJRepo accountParuruJRepo;

    @RequestMapping(value = "/password/edit", method = RequestMethod.POST)
    public ResponseEntity updatePassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String adminToken = adminToken(entity);
        admin(adminToken);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESAccount account = accountJRepo.findByIdAndDeactivateFalse(rget(body, "esaccount", Long.class));
        long accountId = account.getId();

        String newPassword = rget(body, "esnpass");
        ESAccountParuru accountParuru = account.getAccountParuru();

        String newParuru = Random.alphaNumericLowerCase(16);
        byte[] newPasswordBytes = newPassword.getBytes(Charset.forName("UTF-8"));
        byte[] newParuruBytes = newParuru.getBytes(Charset.forName("UTF-8"));
        String newHashedpass = PasswordUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        accountParuru.setParuru(newParuru);

        accountJRepo.saveAndFlush(account);
        accountParuruJRepo.saveAndFlush(accountParuru);

        return okJsonSuccess("edit_password_success");
    }
}
