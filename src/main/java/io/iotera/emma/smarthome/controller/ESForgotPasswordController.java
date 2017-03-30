package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.mail.MailHelper;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountForgotPassword;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/password/forgot")
public class ESForgotPasswordController extends ESBaseController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountForgotPasswordJRepo accountForgotPasswordJRepo;

    @Autowired
    MailHelper mailHelper;

    private ResponseEntity forgotPassword(ESAccount account) {

        String username = account.getUsername();
        String email = account.getEmail();
        String token = Random.alphaNumericLowerCase(128);

        try {
            mailHelper.send(email, "Forgot Emma Password",
                    "Username : " + username + "\n" +
                            "Email : " + email + "\n\n" +
                            "Link : " + getProperty("host.path.remote") + "/password/reset/account/" + token);

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
        accountForgotPasswordJRepo.saveAndFlush(accountForgotPassword);

        // Response
        return okJsonSuccess("forgot_password_success");
    }

    @RequestMapping(value = "/input", method = RequestMethod.POST)
    public ResponseEntity forgotPasswordUsingInput(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String input = rget(body, "esinput");

        ESAccount account = accountJRepo.findByUsernameAndDeactivateFalse(input);
        if (account == null) {
            account = accountJRepo.findByEmailAndDeactivateFalse(input);
            if (account == null) {
                return okJsonFailed(-1, "invalid_username_email_or_password");
            }
        }

        return forgotPassword(account);
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity forgotPasswordUsingToken(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESAccount account = accountClient(clientToken);

        return forgotPassword(account);
    }

}
