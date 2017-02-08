package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.*;
import io.iotera.emma.smarthome.repository.ESAccountRepository.*;
import io.iotera.emma.smarthome.utility.ResourceUtility;
import io.iotera.emma.smarthome.utility.ESUtility;
import io.iotera.util.Json;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/api/hub/auth")
public class ESHubAuthController extends ESBaseController {

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountParuruJpaRepository accountParuruJpaRepository;

    @Autowired
    ESAccountProfileJpaRepository accountProfileJpaRepository;

    @Autowired
    ESAccountLocationJpaRepository accountLocationJpaRepository;

    @Autowired
    ESAccountForgotPasswordJpaRepository accountForgotPasswordJpaRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String email = rget(body, "esemail");
        String password = rget(body, "espass");
        String phoneNumber = rget(body, "esphone");
        String name = rget(body, "esname");
        String firstName = rget(body, "esfname");
        String lastName = rget(body, "eslname");
        String gender = rget(body, "esgender");
        int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
        String dobString = rget(body, "esdob");
        Date dob = parseDate(dobString, "yyyy-MM-dd");
        String address = get(body, "esaddress");
        String latitude = get(body, "eslat");
        String longitude = get(body, "eslong");

        // Check email
        if (accountJpaRepository.findByEmailAndDeactivateFalse(email) != null) {
            return okJsonFailed(-1, "email_not_available");
        }

        /*
        // Check phone number
        if (accountJpaRepository.findByPhoneNumberAndDeactivateFalse(email) != null) {
            return okJsonFailed(-3, "phone_number_not_available");
        }
        */

        String paruru = ESUtility.randomString(16);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
        String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);

        ESAccount account = new ESAccount(email, hashedpass, phoneNumber, name);
        accountJpaRepository.save(account);
        long accountId = account.getId();

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.hubPath(accountId, "account");
            String attachment = getProperty("attachment.path");

            byte[] data = Base64.decodeBase64(picture);
            String filename = ESUtility.randomString(8);
            ResourceUtility.save(data, attachment, path, filename);
            account.setPicture(path + "/" + filename);
            account.setPictureLastUpdated(account.getRegisteredDate());
        }

        ESAccountParuru accountParuru = new ESAccountParuru(paruru, account, accountId);
        ESAccountProfile accountProfile = new ESAccountProfile(firstName, lastName, genderInt, dob,
                account, accountId);
        ESAccountLocation accountLocation = new ESAccountLocation(address, latitude, longitude, account, accountId);

        accountJpaRepository.flush();
        accountParuruJpaRepository.saveAndFlush(accountParuru);
        accountProfileJpaRepository.saveAndFlush(accountProfile);
        accountLocationJpaRepository.saveAndFlush(accountLocation);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("name", account.getName());
        response.put("picture", account.picturePath(getProperty("host.path")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated(),"yyyy-MM-dd HH:mm:ss"));
        response.put("registered_date", formatDate(account.getRegisteredDate(),"yyyy-MM-dd HH:mm:ss"));
        response.put("first_name", accountProfile.getFirstName());
        response.put("last_name", accountProfile.getLastName());
        response.put("gender", accountProfile.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(accountProfile.getDob(),"yyyy-MM-dd HH:mm:ss"));
        response.put("address", accountLocation.getAddress());
        response.put("latitude", accountLocation.getLatitude());
        response.put("longitude", accountLocation.getLongitude());
        response.put("htoken", account.getHubToken());
        response.put("status_desc", "registration_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);

        // Request Body
        ObjectNode body = payloadObject(entity);
        String email = rget(body, "esemail");
        String password = rget(body, "espass");

        ESAccount account = accountJpaRepository.findByEmailAndDeactivateFalse(email);
        if (account == null) {
            return okJsonFailed(-1, "invalid_username_or_password");
        }
        String savedpass = account.getPassword();

        String paruru = account.getAccountParuru().getParuru();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] paruruBytes = paruru.getBytes(StandardCharsets.UTF_8);
        String hashedpass = ESUtility.hashPassword(passwordBytes, paruruBytes);
        if (!savedpass.equals(hashedpass)) {
            return okJsonFailed(-1, "invalid_username_or_password");
        }

        account.generateHubToken();
        accountJpaRepository.saveAndFlush(account);

        ESAccountProfile accountProfile = account.getAccountProfile();
        ESAccountLocation accountLocation = account.getAccountLocation();
        ESAccountCamera accountCamera = account.getAccountCamera();

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("name", account.getName());
        response.put("picture", account.picturePath(getProperty("host.path")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated(),"yyyy-MM-dd HH:mm:ss"));
        response.put("registered_date", formatDate(account.getRegisteredDate(),"yyyy-MM-dd HH:mm:ss"));
        response.put("first_name", accountProfile.getFirstName());
        response.put("last_name", accountProfile.getLastName());
        response.put("gender", accountProfile.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(accountProfile.getDob(),"yyyy-MM-dd HH:mm:ss"));
        response.put("address", accountLocation.getAddress());
        response.put("latitude", accountLocation.getLatitude());
        response.put("longitude", accountLocation.getLongitude());
        if (accountCamera != null) {
            response.put("youtube_id", accountCamera.getYoutubeId());
            response.put("youtube_email", accountCamera.getYoutubeEmail());
            response.put("max_history", accountCamera.getMaxHistory());
        }
        response.put("htoken", account.getHubToken());
        response.put("status_desc", "login_success");
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

        ESAccount account = accountJpaRepository.findByEmailAndDeactivateFalse(email);
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

        ESAccount account = accountJpaRepository.findByPhoneNumberAndDeactivateFalse(phoneNumber);
        if (account != null) {
            return okJsonFailed(-1, "phone_number_not_available");
        }

        // Response
        return okJsonSuccess("phone_number_available");
    }

}
