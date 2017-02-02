package io.iotera.emma.smarthome.controller;

import io.iotera.emma.smarthome.mail.MailHelper;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountForgotPassword;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.client.ESClientForgotPassword;
import io.iotera.emma.smarthome.model.client.ESClientParuru;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountForgotPasswordJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountParuruJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientForgotPasswordJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientParuruJpaRepository;
import io.iotera.emma.smarthome.utility.ESUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/reset/password")
public class ESResetPasswordController extends ESBaseController {

    @Autowired
    MailHelper mailHelper;

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountParuruJpaRepository accountParuruJpaRepository;

    @Autowired
    ESAccountForgotPasswordJpaRepository accountForgotPasswordJpaRepository;

    @RequestMapping(value = "/hub/**", method = RequestMethod.GET)
    public ResponseEntity resetHub(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String token = path.substring("/reset/password/hub/".length());

        ESAccountForgotPassword accountForgotPassword = accountForgotPasswordJpaRepository.findByToken(token);
        if (accountForgotPassword == null) {
            return notFound("");
        }

        ESAccount account = accountForgotPassword.getAccount();
        if (account == null) {
            return notFound("");
        }

        String email = account.getEmail();
        String newPassword = ESUtility.randomString(8);

        try {
            mailHelper.send(email, "New Emma Hub Password",
                    "Email : " + email + "\n\nPassword : " + newPassword);

        } catch (MessagingException e) {
            e.printStackTrace();
            return internalServerError("");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return internalServerError("");
        }

        ESAccountParuru accountParuru = account.getAccountParuru();
        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        account.generateHubToken();
        accountParuru.setParuru(newParuru);
        accountForgotPassword.setAccount(null);

        accountJpaRepository.saveAndFlush(account);
        accountParuruJpaRepository.saveAndFlush(accountParuru);
        accountForgotPasswordJpaRepository.delete(accountForgotPassword);

        return returnText(HttpStatus.OK, "New password sent to email");
    }

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientParuruJpaRepository clientParuruJpaRepository;

    @Autowired
    ESClientForgotPasswordJpaRepository clientForgotPasswordJpaRepository;

    @RequestMapping(value = "/client/**", method = RequestMethod.GET)
    public ResponseEntity resetClient(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String token = path.substring("/reset/password/client/".length());

        ESClientForgotPassword clientForgotPassword = clientForgotPasswordJpaRepository.findByToken(token);
        if (clientForgotPassword == null) {
            return notFound("");
        }

        ESClient client = clientForgotPassword.getClient();
        if (client == null) {
            return notFound("");
        }

        String email = client.getEmail();
        String newPassword = ESUtility.randomString(8);

        try {
            mailHelper.send(email, "New Emma Client Password",
                    "Email : " + email + "\n\nPassword : " + newPassword);

        } catch (MessagingException e) {
            e.printStackTrace();
            return internalServerError("");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return internalServerError("");
        }

        ESClientParuru clientParuru = client.getClientParuru();
        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        client.setPassword(newHashedpass);
        client.generateClientToken();
        clientParuru.setParuru(newParuru);
        clientForgotPassword.setClient(null);

        clientJpaRepository.saveAndFlush(client);
        clientParuruJpaRepository.saveAndFlush(clientParuru);
        clientForgotPasswordJpaRepository.delete(clientForgotPassword);

        return returnText(HttpStatus.OK, "New password sent to email");
    }

}
