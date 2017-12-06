package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
//import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.PasswordUtility;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.util.Json;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;

@RestController
@RequestMapping("/client/auth")
public class ESClientAuthController extends ESBaseController implements ApplicationEventPublisherAware {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountParuruJRepo accountParuruJRepo;

    @Autowired
    ESAccountRepo.ESAccountForgotPasswordJRepo accountForgotPasswordJRepo;

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @RequestMapping(value = "/register/{method}", method = RequestMethod.POST)
    public ResponseEntity register(
            @PathVariable("method") String method,
            HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String username = rget(body, "esuser");
        String email = rget(body, "esemail");
        String password = rget(body, "espass");
        String phoneNumber = rget(body, "esphone");
        String first = get(body, "esfirst");
        String last = get(body, "eslast");

        String googleId = null;
        String facebookId = null;

        if (method.equalsIgnoreCase("google")) {
            googleId = rget(body, "esgoogle");
            if (accountJRepo.findByGoogleIdAndDeactivateFalse(googleId) != null) {
                return okJsonFailed(-4, "google_id_not_available");
            }
        } else if (method.equalsIgnoreCase("facebook")) {
            facebookId = rget(body, "esfacebook");
            if (accountJRepo.findByFacebookIdAndDeactivateFalse(facebookId) != null) {
                return okJsonFailed(-5, "facebook_id_not_available");
            }
        } else if (method.equalsIgnoreCase("username")) {
            // continue
        } else {
            return notFound("");
        }

        // Check username
        if (accountJRepo.findByUsernameAndDeactivateFalse(username) != null) {
            return okJsonFailed(-1, "username_not_available");
        }

        // Check email
        if (accountJRepo.findByEmailAndDeactivateFalse(email) != null) {
            return okJsonFailed(-2, "email_not_available");
        }

        /*
        // Check phone number
        if (accountJpaRepository.findByPhoneNumberAndDeactivateFalse(email) != null) {
            return okJsonFailed(-3, "phone_number_not_available");
        }
        */

        String paruru = Random.alphaNumericLowerCase(16);
        byte[] passwordBytes = password.getBytes(Charset.forName("UTF-8"));
        byte[] paruruBytes = paruru.getBytes(Charset.forName("UTF-8"));
        String hashedpass = PasswordUtility.hashPassword(passwordBytes, paruruBytes);

        ESAccount account = new ESAccount(email, username, hashedpass, phoneNumber, googleId, facebookId);
        if (first != null) {
            account.setFirstName(first);
        }
        if (last != null) {
            account.setFirstName(last);
        }
        accountJRepo.save(account);
        long accountId = account.getId();

        ESAccountParuru accountParuru = new ESAccountParuru(paruru, account, accountId);

        accountJRepo.flush();
        accountParuruJRepo.saveAndFlush(accountParuru);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("username", account.getUsername());
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("registered_time", formatDate(account.getRegisteredTime()));
        response.put("ctoken", account.getClientToken());
        response.put("first_name", account.getFirstName());
        response.put("last_name", account.getLastName());
        response.put("gender", account.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(account.getDob()));
        response.put("picture", account.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated()));
        response.put("status_desc", "registration_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/login/{method}", method = RequestMethod.POST)
    protected ResponseEntity login(
            @PathVariable("method") String method,
            HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        ESAccount account;
        if (method.equalsIgnoreCase("google")) {
            String googleId = rget(body, "esgoogle");
            account = accountJRepo.findByGoogleIdAndDeactivateFalse(googleId);
            if (account == null) {
                return okJsonFailed(-2, "google_id_not_found");
            }

        } else if (method.equalsIgnoreCase("facebook")) {
            String facebookId = rget(body, "esfacebook");
            account = accountJRepo.findByFacebookIdAndDeactivateFalse(facebookId);
            if (account == null) {
                return okJsonFailed(-3, "facebook_id_not_found");
            }

        } else if (method.equalsIgnoreCase("input")) {

            String input = rget(body, "esinput");
            String password = rget(body, "espass");

            account = accountJRepo.findByUsernameAndDeactivateFalse(input);
            if (account == null) {
                account = accountJRepo.findByEmailAndDeactivateFalse(input);
                if (account == null) {
                    return okJsonFailed(-1, "invalid_username_email_or_password");
                }
            }
            String savedpass = account.getPassword();
            String paruru = account.getAccountParuru().getParuru();

            byte[] passwordBytes = password.getBytes(Charset.forName("UTF-8"));
            byte[] paruruBytes = paruru.getBytes(Charset.forName("UTF-8"));
            String hashedpass = PasswordUtility.hashPassword(passwordBytes, paruruBytes);
            if (!savedpass.equals(hashedpass)) {
                return okJsonFailed(-1, "invalid_username_email_or_password");
            }

        } else {
            return notFound("");
        }


        if (applicationEventPublisher != null) {
            Message<String> sendMessage = MessageBuilder
                    .withPayload("")
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topicClient(
                                    account.getId(), CommandPref.FORCE_LOGOUT, null))
                    .build();

//            applicationEventPublisher.publishEvent(new MqttPublishEvent(
//                    this, CommandPref.FORCE_LOGOUT, sendMessage));
        }

        account.generateClientToken();
        accountJRepo.saveAndFlush(account);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("username", account.getUsername());
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("registered_time", formatDate(account.getRegisteredTime()));
        response.put("ctoken", account.getClientToken());
        response.put("first_name", account.getFirstName());
        response.put("last_name", account.getLastName());
        response.put("gender", account.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(account.getDob()));
        response.put("picture", account.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated()));
        response.put("status_desc", "login_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/check/username", method = RequestMethod.POST)
    public ResponseEntity checkUsername(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String username = rget(body, "esuser");

        ESAccount account = accountJRepo.findByUsernameAndDeactivateFalse(username);
        if (account != null) {
            return okJsonFailed(-1, "username_not_available");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("status_desc", "email_available");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/check/email", method = RequestMethod.POST)
    public ResponseEntity checkEmail(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String email = rget(body, "esemail");

        ESAccount account = accountJRepo.findByEmailAndDeactivateFalse(email);
        if (account != null) {
            return okJsonFailed(-1, "email_not_available");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("status_desc", "email_available");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/check/phonenumber", method = RequestMethod.POST)
    public ResponseEntity checkPhoneNumber(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String phoneNumber = rget(body, "esphone");

        ESAccount account = accountJRepo.findByPhoneNumberAndDeactivateFalse(phoneNumber);
        if (account != null) {
            return okJsonFailed(-1, "phone_number_not_available");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("status_desc", "phone_number_available");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

}
