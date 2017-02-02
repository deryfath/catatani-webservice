package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.mail.MailHelper;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountForgotPassword;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountForgotPasswordJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountParuruJpaRepository;
import io.iotera.emma.smarthome.utility.ESUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/hub/forgot/password")
public class ESHubForgotPasswordController extends ESBaseController {

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountParuruJpaRepository accountParuruJpaRepository;

    @Autowired
    ESAccountForgotPasswordJpaRepository accountForgotPasswordJpaRepository;

    @Autowired
    MailHelper mailHelper;

    protected ResponseEntity forgotPassword(ESAccount account) {

        String email = account.getEmail();
        String token = ESUtility.randomString(128);

        try {
            mailHelper.send(email, "Forgot Emma Hub Password",
                    "Email : " + email +
                            "\n\nLink : " + getProperty("host.path") + "/reset/password/hub/" + token);

        } catch (MessagingException e) {
            //e.printStackTrace();
            return okJsonFailed(-2, "send_email_failed");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            return okJsonFailed(-2, "send_email_failed");
        }

        ESAccountForgotPassword accountForgotPassword = account.getAccountForgotPassword();
        if (accountForgotPassword == null) {
            accountForgotPassword = new ESAccountForgotPassword(account, account.getId());
        }
        accountForgotPassword.setToken(token);
        accountForgotPasswordJpaRepository.saveAndFlush(accountForgotPassword);

        // Response
        return okJsonSuccess("forgot_password_success");

    }

    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public ResponseEntity forgotPasswordUsingEmail(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String email = rget(body, "esemail");

        ESAccount account = accountJpaRepository.findByEmailAndDeactivateFalse(email);
        if (account == null) {
            return okJsonFailed(-1, "email_not_registered");
        }

        return forgotPassword(account);
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity forgotPasswordUsingToken(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Account
        ESAccount account = accountHub(hubToken);

        return forgotPassword(account);
    }

}
