package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.mail.MailHelper;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.client.ESClientForgotPassword;
import io.iotera.emma.smarthome.model.client.ESClientParuru;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientForgotPasswordJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientParuruJpaRepository;
import io.iotera.emma.smarthome.utility.ESUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
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
@RequestMapping("/api/client/forgot/password")
public class ESClientForgotPasswordController extends ESBaseController {

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientParuruJpaRepository clientParuruJpaRepository;

    @Autowired
    ESClientForgotPasswordJpaRepository clientForgotPasswordJpaRepository;

    @Autowired
    MailHelper mailHelper;

    private ResponseEntity forgotPassword(ESClient client) {

        String email = client.getEmail();
        String token = ESUtility.randomString(128);
        try {
            mailHelper.send(email, "Forgot Emma Client Password",
                    "Email : " + email +
                            "\n\nLink : " + getProperty("host.path") + "/reset/password/client/" + token);

        } catch (MessagingException e) {
            //e.printStackTrace();
            return okJsonFailed(-2, "send_email_failed");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            return okJsonFailed(-2, "send_email_failed");
        }

        ESClientForgotPassword clientForgotPassword = client.getClientForgotPassword();
        if (clientForgotPassword == null) {
            clientForgotPassword = new ESClientForgotPassword(client, client.getId());
        }
        clientForgotPassword.setToken(token);
        clientForgotPasswordJpaRepository.saveAndFlush(clientForgotPassword);

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

        // Client
        ESClient client = clientJpaRepository.findByEmailAndDeactivateFalse(email);
        if (client == null) {
            return okJsonFailed(-1, "email_not_registered");
        }

        return forgotPassword(client);
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity forgotPasswordUsingToken(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Client
        ESClient client = client(clientToken);

        return forgotPassword(client);
    }

    @RequestMapping(value = "/reset/**", method = RequestMethod.GET)
    public ResponseEntity reset(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String token = path.substring("/api/hub/forgot/password/reset".length());

        ESClientForgotPassword clientForgotPassword = clientForgotPasswordJpaRepository.findByToken(token);
        if (clientForgotPassword == null) {
            return notFound("");
        }

        ESClient client = clientForgotPassword.getClient();
        if (client == null) {
            return notFound("");
        }

        String newPassword = ESUtility.randomString(8);

        ESClientParuru clientParuru = client.getClientParuru();
        String newParuru = ESUtility.randomString(16);
        byte[] newPasswordBytes = newPassword.getBytes(StandardCharsets.UTF_8);
        byte[] newParuruBytes = newParuru.getBytes(StandardCharsets.UTF_8);
        String newHashedpass = ESUtility.hashPassword(newPasswordBytes, newParuruBytes);

        client.setPassword(newHashedpass);
        clientParuru.setParuru(newParuru);

        clientJpaRepository.saveAndFlush(client);
        clientParuruJpaRepository.saveAndFlush(clientParuru);
        clientForgotPasswordJpaRepository.delete(clientForgotPassword);

        String output = "Email : " + client.getEmail() + "\nNew Password : " + newPassword;

        return returnText(HttpStatus.OK, output);
    }

}
