package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAccountController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountLocation;
import io.iotera.emma.smarthome.model.account.ESAccountProfile;
import io.iotera.emma.smarthome.repository.ESAccountRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountLocationJpaRepository;
import io.iotera.emma.smarthome.repository.ESAccountRepository.ESAccountProfileJpaRepository;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/hub/account")
public class ESHubAccountController extends ESAccountController {

    @Autowired
    ESAccountJpaRepository accountJpaRepository;

    @Autowired
    ESAccountProfileJpaRepository accountProfileJpaRepository;

    @Autowired
    ESAccountLocationJpaRepository accountLocationJpaRepository;

    @RequestMapping(value = "/get/{attr}", method = RequestMethod.GET)
    public ResponseEntity read(@PathVariable String[] attr, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        List<String> attrList = Arrays.asList(attr);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("registered_date", formatDate(account.getRegisteredDate(), "yyyy-MM-dd HH:mm:ss"));
        response.put("name", account.getName());
        response.put("picture", account.picturePath(getProperty("host.path")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        if (attrList.contains("pro")) {
            ESAccountProfile accountProfile = account.getAccountProfile();
            response.put("first_name", accountProfile.getFirstName());
            response.put("last_name", accountProfile.getLastName());
            response.put("gender", accountProfile.getGender() == 1 ? "female" : "male");
            response.put("dob", formatDate(accountProfile.getDob(), "yyyy-MM-dd HH:mm:ss"));
        }
        if (attrList.contains("loc")) {
            ESAccountLocation accountLocation = account.getAccountLocation();
            response.put("address", accountLocation.getAddress());
            response.put("latitude", accountLocation.getLatitude());
            response.put("longitude", accountLocation.getLongitude());
        }
        response.put("status_desc", "get_account_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        ObjectNode response = Json.buildObjectNode();
        boolean edit = false;

        if (has(body, "esname")) {
            String name = get(body, "esname");
            if (!account.getName().equals(name)) {
                account.setName(name);
                edit = true;
            }
            response.put("name", name);
        }

        // Account Profile
        if (has(body, "esfname") || has(body, "eslname") || has(body, "esgender") || has(body, "esdob")) {
            ESAccountProfile accountProfile = account.getAccountProfile();
            boolean editProfile = false;

            if (has(body, "esfname")) {
                String firstName = get(body, "esfname");
                if (!accountProfile.equals(firstName)) {
                    accountProfile.setFirstName(firstName);
                    editProfile = true;
                }
                response.put("first_name", firstName);
            }

            if (has(body, "eslname")) {
                String lastName = get(body, "eslname");
                if (!accountProfile.getLastName().equals(lastName)) {
                    accountProfile.setLastName(lastName);
                    editProfile = true;
                }
                response.put("last_name", lastName);
            }

            if (has(body, "esgender")) {
                String gender = get(body, "esgender");
                int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
                if (accountProfile.getGender() != genderInt) {
                    accountProfile.setGender(genderInt);
                    editProfile = true;
                }
                response.put("gender", gender);
            }

            if (has(body, "esdob")) {
                String dobString = get(body, "esdob");
                Date dob = parseDate(dobString, "yyyy-MM-dd");
                if (accountProfile.getDob() != dob) {
                    accountProfile.setDob(dob);
                    editProfile = true;
                }
                response.put("dob", dobString);
            }

            if (editProfile) {
                accountProfileJpaRepository.saveAndFlush(accountProfile);
            }
        }

        // Account Location
        if (has(body, "esaddress") || has(body, "eslat") || has(body, "eslong")) {
            ESAccountLocation accountLocation = account.getAccountLocation();
            boolean editLocation = false;

            if (has(body, "esaddress")) {
                String address = get(body, "esaddress");
                if (!address.equals(accountLocation.getAddress())) {
                    accountLocation.setAddress(address);
                    editLocation = true;
                }
                response.put("address", address);
            }

            if (has(body, "eslat")) {
                String latitude = get(body, "eslat");
                if (!latitude.equals(accountLocation.getLatitude())) {
                    accountLocation.setLatitude(latitude);
                    editLocation = true;
                }
                response.put("latitude", latitude);
            }

            if (has(body, "eslong")) {
                String longitude = get(body, "eslong");
                if (!longitude.equals(accountLocation.getLongitude())) {
                    accountLocation.setLongitude(longitude);
                    editLocation = true;
                }
                response.put("longitude", longitude);
            }

            if (editLocation) {
                accountLocationJpaRepository.saveAndFlush(accountLocation);
            }
        }

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.hubPath(accountId, "account");
            String attachment = getProperty("attachment.path");

            // Delete current picture
            if (account.getPicture() != null) {
                String filename = ResourceUtility.filename(account.getPicture());
                ResourceUtility.delete(attachment, path, filename);
            }
            if (!picture.isEmpty()) {
                // Update
                byte[] data = Base64.decodeBase64(picture);
                String newFilename = ESUtility.randomString(8);
                ResourceUtility.save(data, attachment, path, newFilename);
                account.setPicture(path + "/" + newFilename);
            } else {
                // Delete
                account.setPicture(null);
            }
            account.setPictureLastUpdated(new Date());
            edit = true;

            response.put("picture", account.picturePath(getProperty("host.path")));
            response.put("picture_last_updated", formatDate(account.getPictureLastUpdated(), "yyyy-MM-dd HH:mm:ss"));
        }

        if (edit) {
            accountJpaRepository.saveAndFlush(account);
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
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        // Response
        return updateHubPassword(body, account);
    }

}
