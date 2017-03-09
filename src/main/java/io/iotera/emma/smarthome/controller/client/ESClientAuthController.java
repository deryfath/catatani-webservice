package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.client.ESClientParuru;
import io.iotera.emma.smarthome.model.client.ESClientProfile;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientParuruJpaRepository;
import io.iotera.emma.smarthome.repository.ESClientRepository.ESClientProfileJpaRepository;
import io.iotera.emma.smarthome.utility.ESUtility;
import io.iotera.emma.smarthome.utility.ResourceUtility;
import io.iotera.util.Json;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/api/client/auth")
public class ESClientAuthController extends ESBaseController {

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientParuruJpaRepository clientParuruJpaRepository;

    @Autowired
    ESClientProfileJpaRepository clientProfileJpaRepository;

    @RequestMapping(value = "/register/{method}", method = RequestMethod.POST)
    public ResponseEntity register(
            @PathVariable("method") String method, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        String username = rget(body, "esuser");
        String password = rget(body, "espass");
        String email = rget(body, "esemail");
        String phoneNumber = get(body, "esphone");
        String firstName = rget(body, "esfname");
        String lastName = rget(body, "eslname");
        String gender = rget(body, "esgender");
        int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
        String dobString = rget(body, "esdob");
        Date dob = parseDate(dobString, "yyyy-MM-dd");

        String googleId = null;
        String facebookId = null;

        if (method.equals("google")) {
            googleId = rget(body, "esgoogle");
            if (clientJpaRepository.findByGoogleIdAndDeactivateFalse(googleId) != null) {
                return okJsonFailed(-4, "google_id_not_available");
            }
        } else if (method.equals("facebook")) {
            facebookId = rget(body, "esfacebook");
            if (clientJpaRepository.findByFacebookIdAndDeactivateFalse(facebookId) != null) {
                return okJsonFailed(-5, "facebook_id_not_available");
            }
        } else if (method.equals("username")) {
            // continue
        } else {
            return notFound("");
        }

        // Check email
        if (clientJpaRepository.findByEmailAndDeactivateFalse(email) != null) {
            return okJsonFailed(-1, "email_not_available");
        }

        // Check username
        if (clientJpaRepository.findByUsernameAndDeactivateFalse(username) != null) {
            return okJsonFailed(-2, "username_not_available");
        }

        // Check phone number
        /*
        if (clientJpaRepository.findByPhoneNumberAndDeactivateFalse(username) != null) {
            return okJsonFailed(-3, "phone_number_not_available");
        }
        */

        String paruru = ESUtility.randomString(16);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
        String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);

        ESClient client = new ESClient(username, hashedpass, email, phoneNumber, firstName, lastName, googleId, facebookId);
        clientJpaRepository.save(client);
        long clientId = client.getId();

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.clientPath(clientId, "client");
            String attachment = getProperty("attachment.path");

            byte[] data = Base64.decodeBase64(picture);
            String filename = ESUtility.randomString(8);
            ResourceUtility.save(data, attachment, path, filename);
            client.setPicture(path + "/" + filename);
            client.setPictureLastUpdated(client.getRegisteredDate());
        }

        ESClientParuru clientParuru = new ESClientParuru(paruru, client, clientId);
        ESClientProfile clientProfile = new ESClientProfile(genderInt, dob, client, clientId);

        clientJpaRepository.flush();
        clientParuruJpaRepository.saveAndFlush(clientParuru);
        clientProfileJpaRepository.saveAndFlush(clientProfile);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(client.getId()));
        response.put("username", client.getUsername());
        response.put("email", client.getEmail());
        response.put("phone_number", client.getPhoneNumber());
        response.put("first_name", client.getFirstName());
        response.put("last_name", client.getLastName());
        response.put("registered_date", formatDate(client.getRegisteredDate(), "yyyy-MM-dd HH:mm:ss"));
        response.put("gender", clientProfile.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(clientProfile.getDob(), "yyyy-MM-dd HH:mm:ss"));
        response.put("picture", client.picturePath(getProperty("host.path")));
        response.put("picture_last_updated", formatDate(client.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        response.put("ctoken", client.getClientToken());
        response.put("status_desc", "register_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/login/{method}", method = RequestMethod.POST)
    public ResponseEntity login(
            @PathVariable("method") String method, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);

        ESClient client;
        if (method.equals("google")) {
            String googleId = rget(body, "esgoogle");
            client = clientJpaRepository.findByGoogleIdAndDeactivateFalse(googleId);
            if (client == null) {
                return okJsonFailed(-2, "google_id_not_found");
            }

        } else if (method.equals("facebook")) {
            String facebookId = rget(body, "esfacebook");
            client = clientJpaRepository.findByFacebookIdAndDeactivateFalse(facebookId);
            if (client == null) {
                return okJsonFailed(-3, "facebook_id_not_found");
            }

        } else if (method.equals("input")) {
            String input = get(body, "esinput");
            String password = rget(body, "espass");

            client = clientJpaRepository.findByUsernameAndDeactivateFalse(input);
            if (client == null) {
                client = clientJpaRepository.findByEmailAndDeactivateFalse(input);
                if (client == null) {
                    return okJsonFailed(-1, "invalid_username_email_or_password");
                }
            }

            String savedpass = client.getPassword();
            String paruru = client.getClientParuru().getParuru();

            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
            String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);
            if (!savedpass.equals(hashedpass)) {
                return okJsonFailed(-1, "invalid_username_email_or_password");
            }

        } else {
            return notFound("");
        }

        client.generateClientToken();
        clientJpaRepository.saveAndFlush(client);

        ESClientProfile clientProfile = client.getClientProfile();

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(client.getId()));
        response.put("username", client.getUsername());
        response.put("email", client.getEmail());
        response.put("phone_number", client.getPhoneNumber());
        response.put("first_name", client.getFirstName());
        response.put("last_name", client.getLastName());
        response.put("registered_date", formatDate(client.getRegisteredDate(), "yyyy-MM-dd HH:mm:ss"));
        response.put("gender", clientProfile.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(clientProfile.getDob(), "yyyy-MM-dd HH:mm:ss"));
        response.put("picture", client.picturePath(getProperty("host.path")));
        response.put("picture_last_updated", formatDate(client.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        response.put("ctoken", client.getClientToken());
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

        ESClient client = clientJpaRepository.findByUsernameAndDeactivateFalse(username);
        if (client != null) {
            return okJsonFailed(-1, "username_not_available");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();

        response.put("status_desc", "username_available");
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

        ESClient client = clientJpaRepository.findByEmailAndDeactivateFalse(email);
        if (client != null) {
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

        ESClient client = clientJpaRepository.findByPhoneNumberAndDeactivateFalse(phoneNumber);
        if (client != null) {
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
