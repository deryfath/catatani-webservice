package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Base64;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountClient;
import io.iotera.emma.smarthome.model.account.ESAccountHub;
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
import java.util.List;

public class ESAccountController extends ESBaseController {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccountRepo.ESAccountParuruJRepo accountParuruJRepo;

    @Autowired
    ESAccountRepo.ESAccountClientJRepo accountClientJRepo;

    @Autowired
    ESAccountRepo.ESAccountHubJRepo accountHubJRepo;

    protected ResponseEntity read(List<String> attrList, ESAccount account) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(account.getId()));
        response.put("email", account.getEmail());
        response.put("username", account.getUsername());
        response.put("phone_number", account.getPhoneNumber());
        if (attrList.contains("client")) {
            response.put("registered_time", formatDate(account.getRegisteredTime()));
            ESAccountClient accountClient = account.getAccountClient();
            response.put("first_name", accountClient.getFirstName());
            response.put("last_name", accountClient.getLastName());
            response.put("gender", accountClient.getGender() == 1 ? "female" : "male");
            response.put("dob", formatDate(accountClient.getDob()));
            response.put("picture", accountClient.picturePath(getProperty("host.path.remote")));
            response.put("picture_last_updated", formatDate(accountClient.getPictureLastUpdated()));
        } else if (attrList.contains("hub")) {
            ESAccountHub accountHub = account.getAccountHub();
            response.put("name", accountHub.getName());
            response.put("address", accountHub.getAddress());
            response.put("latitude", accountHub.getLatitude());
            response.put("longitude", accountHub.getLongitude());
            if (attrList.contains("client")) {
                response.put("hub_picture", accountHub.picturePath(getProperty("host.path.remote")));
                response.put("hub_picture_last_updated", formatDate(accountHub.getPictureLastUpdated()));
            } else {
                response.put("picture", accountHub.picturePath(getProperty("host.path.remote")));
                response.put("picture_last_updated", formatDate(accountHub.getPictureLastUpdated()));
            }
        }
        response.put("status_desc", "get_client_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity update(ObjectNode body, boolean client, ESAccount account, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        boolean edit = false;

        if (client) {
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
                response.put("username", username);
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
                response.put("email", email);
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
                response.put("phone_number", phoneNumber);
            }

            // Account Client
            if (has(body, "esfname") || has(body, "eslname") ||
                    has(body, "esgender") || has(body, "esdob") ||
                    has(body, "espic")) {
                ESAccountClient accountClient = account.getAccountClient();
                boolean editClient = false;

                if (has(body, "esfname")) {
                    String firstName = get(body, "esfname");
                    if (!accountClient.getFirstName().equals(firstName)) {
                        accountClient.setFirstName(firstName);
                        editClient = true;
                    }
                    response.put("first_name", firstName);
                }

                if (has(body, "eslname")) {
                    String lastName = get(body, "eslname");
                    if (!accountClient.getLastName().equals(lastName)) {
                        accountClient.setLastName(lastName);
                        editClient = true;
                    }
                    response.put("last_name", lastName);
                }

                if (has(body, "esgender")) {
                    String gender = get(body, "esgender");
                    int genderInt = gender.equalsIgnoreCase("female") ? 1 : 2;
                    if (accountClient.getGender() != genderInt) {
                        accountClient.setGender(genderInt);
                        editClient = true;
                    }
                    response.put("gender", gender);
                }

                if (has(body, "esdob")) {
                    String dobString = get(body, "esdob");
                    Date dob = parseDate(dobString, "yyyy-MM-dd");
                    if (accountClient.getDob() != dob) {
                        accountClient.setDob(dob);
                        editClient = true;
                    }
                    response.put("dob", formatDate(dob));
                }

                if (has(body, "espic")) {
                    String picture = get(body, "espic");
                    String path = ResourceUtility.clientPath(accountId, "client");
                    String attachment = getProperty("attachment.path");

                    // Delete current picture
                    if (accountClient.getPicture() != null) {
                        String filename = ResourceUtility.filename(accountClient.getPicture());
                        ResourceUtility.delete(attachment, path, filename);
                    }
                    if (!picture.isEmpty()) {
                        // Update
                        byte[] data = Base64.decodeBase64(picture);
                        String newFilename = Random.alphaNumericLowerCase(8);
                        ResourceUtility.save(data, attachment, path, newFilename);
                        accountClient.setPicture(path + "/" + newFilename);
                    } else {
                        // Delete
                        accountClient.setPicture(null);
                    }
                    accountClient.setPictureLastUpdated(new Date());
                    editClient = true;

                    response.put("picture", accountClient.picturePath(getProperty("host.path.remote")));
                    response.put("picture_last_updated", formatDate(accountClient.getPictureLastUpdated()));
                }

                if (editClient) {
                    accountClientJRepo.saveAndFlush(accountClient);
                }
            }
        } else {
            // Account Hub
            if (!account.isHubActive()) {
                return okJsonFailed(-1, "hub_not_active");
            }

            if (has(body, "esname") || has(body, "esaddress") ||
                    has(body, "eslat") || has(body, "eslong") ||
                    has(body, "espic")) {
                ESAccountHub accountHub = account.getAccountHub();
                boolean editHub = false;

                if (has(body, "esname")) {
                    String name = get(body, "esname");
                    if (!accountHub.getName().equals(name)) {
                        accountHub.setName(name);
                        editHub = true;
                    }
                    response.put("name", name);
                }

                if (has(body, "esaddress")) {
                    String address = get(body, "esaddress");
                    if (!accountHub.getAddress().equals(address)) {
                        accountHub.setAddress(address);
                        editHub = true;
                    }
                    response.put("address", address);
                }

                if (has(body, "eslat")) {
                    String latitude = get(body, "eslat");
                    if (!accountHub.getLatitude().equals(latitude)) {
                        accountHub.setLatitude(latitude);
                        editHub = true;
                    }
                    response.put("latitude", latitude);
                }

                if (has(body, "eslong")) {
                    String longitude = get(body, "eslong");
                    if (!accountHub.getLongitude().equals(longitude)) {
                        accountHub.setLongitude(longitude);
                        editHub = true;
                    }
                    response.put("longitude", longitude);
                }

                if (has(body, "espic")) {
                    String picture = get(body, "espic");
                    String path = ResourceUtility.clientPath(accountId, "client");
                    String attachment = getProperty("attachment.path");

                    // Delete current picture
                    if (accountHub.getPicture() != null) {
                        String filename = ResourceUtility.filename(accountHub.getPicture());
                        ResourceUtility.delete(attachment, path, filename);
                    }
                    if (!picture.isEmpty()) {
                        // Update
                        byte[] data = Base64.decodeBase64(picture);
                        String newFilename = Random.alphaNumericLowerCase(8);
                        ResourceUtility.save(data, attachment, path, newFilename);
                        accountHub.setPicture(path + "/" + newFilename);
                    } else {
                        // Delete
                        accountHub.setPicture(null);
                    }
                    accountHub.setPictureLastUpdated(new Date());
                    editHub = true;

                    response.put("picture", accountHub.picturePath(getProperty("host.path.remote")));
                    response.put("picture_last_updated", formatDate(accountHub.getPictureLastUpdated()));
                }

                if (editHub) {
                    accountHubJRepo.saveAndFlush(accountHub);
                }
            }
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
