package io.iotera.emma.smarthome.controller;

import io.iotera.emma.smarthome.mail.MailHelper;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountForgotPassword;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.PasswordUtility;
import io.iotera.util.Random;
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
import java.nio.charset.Charset;

@RestController
@RequestMapping("/password/reset")
public class ESResetPasswordController extends ESBaseController {

    @Autowired
    MailHelper mailHelper;

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountParuruJRepo accountParuruJRepo;

    @Autowired
    ESAccountRepo.ESAccountForgotPasswordJRepo accountForgotPasswordJRepo;

    @RequestMapping(value = "/account/**", method = RequestMethod.GET)
    public ResponseEntity resetAccount(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String token = path.substring("/password/reset/account/".length());

        ESAccountForgotPassword accountForgotPassword = accountForgotPasswordJRepo.findByToken(token);
        if (accountForgotPassword == null) {
            return notFound("");
        }

        ESAccount account = accountForgotPassword.getAccount();
        if (account == null) {
            return notFound("");
        }

        String email = account.getEmail();
        String username = account.getUsername();
        String newPassword = Random.alphaNumericLowerCase(8);

        ESAccountParuru accountParuru = account.getAccountParuru();
        String newParuru = Random.alphaNumericLowerCase(16);
        byte[] newPasswordBytes = newPassword.getBytes(Charset.forName("UTF-8"));
        byte[] newParuruBytes = newParuru.getBytes(Charset.forName("UTF-8"));
        String newHashedpass = PasswordUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        accountParuru.setParuru(newParuru);
        accountForgotPassword.setAccount(null);

        accountJRepo.saveAndFlush(account);
        accountParuruJRepo.saveAndFlush(accountParuru);
        accountForgotPasswordJRepo.delete(accountForgotPassword);

        try {
            mailHelper.send(email, "New Emma Password",
                    "Username : " + username + "\n" +
                            "Email : " + email + "\n\n" +
                            "New Password : " + newPassword);

        } catch (MessagingException e) {
            e.printStackTrace();
            return internalServerError("");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return internalServerError("");
        }

        return returnText(HttpStatus.OK, "New password sent to email");
    }

}
