package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Base64;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountParuru;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.PasswordUtility;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Json;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;
import java.util.Date;

public class ESAccountController extends ESBaseController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountParuruJRepo accountParuruJRepo;

    protected ResponseEntity read(ESAccount account) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("username", account.getUsername());
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("registered_time", formatDate(account.getRegisteredTime()));
        response.put("first_name", account.getFirstName());
        response.put("last_name", account.getLastName());
        response.put("gender", account.getGender() == 1 ? "female" : "male");
        response.put("dob", formatDate(account.getDob()));
        response.put("picture", account.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(account.getPictureLastUpdated()));
        response.put("status_desc", "get_client_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity update(ObjectNode body, ESAccount account, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("username", account.getUsername());
        response.put("email", account.getEmail());
        response.put("phone_number", account.getPhoneNumber());
        response.put("registered_time", formatDate(account.getRegisteredTime()));
        boolean edit = false;

        if (has(body, "esuser")) {
            String username = get(body, "esuser");
            // Check phone number
            if (!account.getUsername().equals(username)) {
                if (accountJRepo.findByUsernameAndDeactivateFalse(username) != null) {
                    return okJsonFailed(-1, "username_not_available");
                }
                account.setUsername(username);
                edit = true;
            }
            response.put("username", account.getUsername());
        }

        if (has(body, "esemail")) {
            String email = get(body, "esemail");
            // Check email
            if (!account.getEmail().equals(email)) {
                if (accountJRepo.findByEmailAndDeactivateFalse(email) != null) {
                    return okJsonFailed(-2, "email_not_available");
                }
                account.setEmail(email);
                edit = true;
            }
            response.put("email", account.getEmail());
        }

        if (has(body, "esphone")) {
            String phoneNumber = get(body, "esphone");
            // Check phone number
            if (!account.getPhoneNumber().equals(phoneNumber)) {
                if (accountJRepo.findByPhoneNumberAndDeactivateFalse(phoneNumber) != null) {
                    return okJsonFailed(-3, "phone_number_not_available");
                }
                account.setPhoneNumber(phoneNumber);
                edit = true;
            }
            response.put("phone_number", account.getPhoneNumber());
        }

        if (has(body, "esfname")) {
            String firstName = get(body, "esfname");
            if (!account.getFirstName().equals(firstName)) {
                account.setFirstName(firstName);
                edit = true;
            }
            response.put("first_name", account.getFirstName());
        }

        if (has(body, "eslname")) {
            String lastName = get(body, "eslname");
            if (!account.getLastName().equals(lastName)) {
                account.setLastName(lastName);
                edit = true;
            }
            response.put("last_name", account.getLastName());
        }

        if (has(body, "esgender")) {
            String gender = get(body, "esgender");
            int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
            if (account.getGender() != genderInt) {
                account.setGender(genderInt);
                edit = true;
            }
            response.put("gender", gender);
        }

        if (has(body, "esdob")) {
            String dobString = get(body, "esdob");
            Date dob = parseDate(dobString, "yyyy-MM-dd");
            if (account.getDob() != dob) {
                account.setDob(dob);
                edit = true;
            }
            response.put("dob", formatDate(dob));
        }

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.clientPath(accountId, "client");
            String attachment = getProperty("attachment.path");

            // Delete current picture
            if (account.getPicture() != null) {
                String filename = ResourceUtility.filename(account.getPicture());
                ResourceUtility.delete(attachment, path, filename);
            }
            if (!picture.isEmpty()) {
                // Update
                byte[] data = Base64.decodeBase64(picture);
                String newFilename = Random.alphaNumericLowerCase(8);
                ResourceUtility.save(data, attachment, path, newFilename);
                account.setPicture(path + "/" + newFilename);
            } else {
                // Delete
                account.setPicture(null);
            }
            account.setPictureLastUpdated(new Date());
            edit = true;

            response.put("picture", account.picturePath(getProperty("host.path.remote")));
            response.put("picture_last_updated", formatDate(account.getPictureLastUpdated()));
        }

        if (edit) {
            accountJRepo.saveAndFlush(account);
        }

        response.put("status_desc", "edit_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity updatePassword(ObjectNode body, ESAccount account) {

        String password = rget(body, "espass");
        String newPassword = rget(body, "esnpass");

        if (password.equals(newPassword)) {
            return okJsonFailed(-2, "password_and_new_password_same");
        }

        ESAccountParuru accountParuru = account.getAccountParuru();

        String savedpass = account.getPassword();
        String paruru = accountParuru.getParuru();
        byte[] passwordBytes = password.getBytes(Charset.forName("UTF-8"));
        byte[] paruruBytes = paruru.getBytes(Charset.forName("UTF-8"));
        String hashedpass = PasswordUtility.hashPassword(passwordBytes, paruruBytes);
        if (!savedpass.equals(hashedpass)) {
            return okJsonFailed(-1, "invalid_password");
        }

        String newParuru = Random.alphaNumericLowerCase(16);
        byte[] newPasswordBytes = newPassword.getBytes(Charset.forName("UTF-8"));
        byte[] newParuruBytes = newParuru.getBytes(Charset.forName("UTF-8"));
        String newHashedpass = PasswordUtility.hashPassword(newPasswordBytes, newParuruBytes);

        account.setPassword(newHashedpass);
        accountParuru.setParuru(newParuru);

        accountJRepo.saveAndFlush(account);
        accountParuruJRepo.saveAndFlush(accountParuru);

        return okJsonSuccess("edit_password_success");
    }

}
