package io.iotera.emma.smarthome.controller.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAccountController;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/client/account")
public class ESClientAccountController extends ESAccountController {

    @Autowired
    ESClientJpaRepository clientJpaRepository;

    @Autowired
    ESClientProfileJpaRepository clientProfileJpaRepository;

    @RequestMapping(value = "/get/{attr}", method = RequestMethod.GET)
    public ResponseEntity read(@PathVariable String[] attr, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        List<String> attrList = Arrays.asList(attr);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("email", client.getEmail());
        if (attrList.contains("pro")) {
            response.put("phone_number", client.getPhoneNumber());
            response.put("first_name", client.getFirstName());
            response.put("last_name", client.getLastName());
            response.put("registered_date", formatDate(client.getRegisteredDate(), "yyyy-MM-dd HH:mm:ss"));
            ESClientProfile clientProfile = client.getClientProfile();
            response.put("gender", clientProfile.getGender() == 1 ? "female" : "male");
            response.put("dob", formatDate(clientProfile.getDob(), "yyyy-MM-dd HH:mm:ss"));
        }
        if (attrList.contains("pic")) {
            response.put("picture", client.picturePath(getProperty("host.path")));
            response.put("picture_last_updated", formatDate(client.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        }
        response.put("status_desc", "get_client_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        ObjectNode response = Json.buildObjectNode();
        boolean edit = false;

        if (has(body, "esemail")) {
            String email = get(body, "esemail");
            // Check email
            if (!client.getEmail().equals(email)) {
                if (clientJpaRepository.findByEmailAndDeactivateFalse(email) != null) {
                    return okJsonFailed(-1, "email_not_available");
                }
                client.setEmail(email);
                edit = true;
            }
            response.put("email", email);
        }

        if (has(body, "esphone")) {
            String phoneNumber = get(body, "esphone");
            // Check phone number
            if (!client.getPhoneNumber().equals(phoneNumber)) {
                if (clientJpaRepository.findByPhoneNumberAndDeactivateFalse(phoneNumber) != null) {
                    return okJsonFailed(-2, "phone_number_not_available");
                }
                client.setPhoneNumber(phoneNumber);
                edit = true;
            }
            response.put("phone_number", phoneNumber);
        }

        if (has(body, "esfname")) {
            String firstName = get(body, "esfname");
            if (!client.getFirstName().equals(firstName)) {
                client.setFirstName(firstName);
                edit = true;
            }
            response.put("first_name", firstName);
        }

        if (has(body, "eslname")) {
            String lastName = get(body, "eslname");
            if (!client.getLastName().equals(lastName)) {
                client.setLastName(lastName);
                edit = true;
            }
            response.put("last_name", lastName);
        }

        // Client Profile
        if (has(body, "esgender") || has(body, "esdob")) {
            ESClientProfile clientProfile = client.getClientProfile();
            boolean editProfile = false;

            if (has(body, "esgender")) {
                String gender = get(body, "esgender");
                int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
                if (clientProfile.getGender() != genderInt) {
                    clientProfile.setGender(genderInt);
                    editProfile = true;
                }
                response.put("gender", gender);
            }

            if (has(body, "esdob")) {
                String dobString = get(body, "esdob");
                Date dob = parseDate(dobString, "yyyy-MM-dd");
                if (clientProfile.getDob() != dob) {
                    clientProfile.setDob(dob);
                    editProfile = true;
                }
                response.put("dob", dobString);
            }

            if (editProfile) {
                clientProfileJpaRepository.saveAndFlush(clientProfile);
            }
        }

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.clientPath(clientId, "client");
            String attachment = getProperty("attachment.path");

            // Delete current picture
            if (client.getPicture() != null) {
                String filename = ResourceUtility.filename(client.getPicture());
                ResourceUtility.delete(attachment, path, filename);
            }
            if (!picture.isEmpty()) {
                // Update
                byte[] data = Base64.decodeBase64(picture);
                String newFilename = ESUtility.randomString(8);
                ResourceUtility.save(data, attachment, path, newFilename);
                client.setPicture(path + "/" + newFilename);
            } else {
                // Delete
                client.setPicture(null);
            }
            client.setPictureLastUpdated(new Date());
            edit = true;

            response.put("picture", client.picturePath(getProperty("host.path")));
            response.put("picture_last_updated", formatDate(client.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        }

        if (edit) {
            clientJpaRepository.saveAndFlush(client);
        }

        response.put("status_desc", "login_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/edit/password", method = RequestMethod.POST)
    public ResponseEntity updatePassword(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String clientToken = clientToken(entity);

        // Client
        ESClient client = client(clientToken);
        long clientId = client.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        return updateClientPassword(body, client);
    }

}
