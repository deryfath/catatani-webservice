package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Base64;
import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESHubRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Json;
import io.iotera.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Date;

public class ESHubController extends ESBaseController implements ApplicationEventPublisherAware {

    @Autowired
    ESHubRepo.ESHubJRepo hubJRepo;

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    protected ResponseEntity check(String uid, String suid) {

        ESHub hub = hubJRepo.findBySuidAndDeactivateFalse(suid);
        if (hub == null) {
            hub = new ESHub(uid, suid);
            hubJRepo.save(hub);
        }

        return login(hub);
    }

    protected ResponseEntity login(ESHub hub) {

        // Response
        ObjectNode response;

        if (hub.isHubActive()) {

            response = hubInfo(hub);

            response.put("status_desc", "hub_active");
            response.put("status_code", 0);
            response.put("status", "success");

            if (applicationEventPublisher != null) {

                String suid = hub.getSuid();

                ObjectNode payload = Json.buildObjectNode();
                payload.put("htoken", hub.getHubToken());

                Message<String> sendMessage = MessageBuilder
                        .withPayload("")
                        .setHeader(MqttHeaders.TOPIC,
                                PublishUtility.topicHubReg(suid, CommandPref.LOGIN))
                        .setHeader(MqttHeaders.RETAINED, true)
                        .build();

                applicationEventPublisher.publishEvent(new MqttPublishEvent(
                        this, CommandPref.FORCE_HOMEKICK, sendMessage));
            }

        } else {

            response = Json.buildObjectNode();

            response.put("rtoken", hub.getRegistrationToken());

            response.put("mqtt_url", getProperty("mqtt.url.remote"));
            response.put("mqtt_username", getProperty("mqtt.username"));
            response.put("mqtt_password", getProperty("mqtt.password"));

            response.put("status_desc", "hub_not_active");
            response.put("status_code", -1);
            response.put("status", "failed");
        }

        return okJson(response);
    }

    protected ResponseEntity logout(ESHub hub) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(hub.getId()));
        response.put("uid", hub.getUid());
        response.put("suid", hub.getSuid());

        Date now = new Date();

        hub.setHubActive(false);
        hub.setDeactivate(true);
        hub.setDeactivateTime(now);
        hub.setHubToken("");
        hub.setRegistrationToken("");

        hubJRepo.saveAndFlush(hub);

        response.put("status_desc", "logout_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity read(ESHub hub) {

        // Response
        ObjectNode response = hubInfo(hub);

        return okJson(response);
    }

    protected ObjectNode hubInfo(ESHub hub) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        response.put("id", String.valueOf(hub.getId()));
        response.put("uid", hub.getUid());
        response.put("suid", hub.getSuid());
        response.put("htoken", hub.getHubToken());
        response.put("hub_active_time", formatDate(hub.getHubActiveTime()));
        response.put("name", hub.getName());
        response.put("address", hub.getAddress());
        response.put("latitude", hub.getLatitude());
        response.put("longitude", hub.getLongitude());
        response.put("picture", hub.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(hub.getPictureLastUpdated()));
        ESAccount client = hub.getClient();
        if (client != null) {
            response.put("client_id", client.getId());
            response.put("username", client.getUsername());
            response.put("email", client.getEmail());
            response.put("phone_number", client.getPhoneNumber());
            response.put("registered_time", formatDate(client.getRegisteredTime()));
            response.put("first_name", client.getFirstName());
            response.put("last_name", client.getLastName());
            response.put("gender", client.getGender() == 1 ? "female" : "male");
            response.put("dob", formatDate(client.getDob()));
            response.put("client_picture", client.picturePath(getProperty("host.path.remote")));
            response.put("client_picture_last_updated", formatDate(client.getPictureLastUpdated()));

            ESAccess access = ESAccess.buildOwnerAccess(hub.getId(), client.getId(), hub.getHubActiveTime());
            response.put("permission", access.permission());
            response.put("added_time", formatDate(access.getAddedTime()));
        }

        return response;
    }

    protected ResponseEntity update(ObjectNode body, ESHub hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", String.valueOf(hub.getId()));
        response.put("uid", hub.getUid());
        response.put("suid", hub.getSuid());
        response.put("hub_active_time", formatDate(hub.getHubActiveTime()));
        boolean edit = false;

        if (has(body, "esname")) {
            String name = get(body, "esname");
            if (!hub.getName().equals(name)) {
                hub.setName(name);
                edit = true;
            }
            response.put("name", hub.getName());
        }

        if (has(body, "esaddress")) {
            String address = get(body, "esaddress");
            if (!hub.getAddress().equals(address)) {
                hub.setAddress(address);
                edit = true;
            }
            response.put("address", hub.getAddress());
        }

        if (has(body, "eslat")) {
            String latitude = get(body, "eslat");
            if (!hub.getLatitude().equals(latitude)) {
                hub.setLatitude(latitude);
                edit = true;
            }
            response.put("latitude", hub.getLatitude());
        }

        if (has(body, "eslong")) {
            String longitude = get(body, "eslong");
            if (!hub.getLongitude().equals(longitude)) {
                hub.setLongitude(longitude);
                edit = true;
            }
            response.put("longitude", hub.getLongitude());
        }

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.hubPath(hubId, "hub");
            String attachment = getProperty("attachment.path");

            // Delete current picture
            if (hub.getPicture() != null) {
                String filename = ResourceUtility.filename(hub.getPicture());
                ResourceUtility.delete(attachment, path, filename);
            }
            if (!picture.isEmpty()) {
                // Update
                byte[] data = Base64.decodeBase64(picture);
                String newFilename = Random.alphaNumericLowerCase(8);
                ResourceUtility.save(data, attachment, path, newFilename);
                hub.setPicture(path + "/" + newFilename);
            } else {
                // Delete
                hub.setPicture(null);
            }
            hub.setPictureLastUpdated(new Date());
            edit = true;

            response.put("picture", hub.picturePath(getProperty("host.path.remote")));
            response.put("picture_last_updated", formatDate(hub.getPictureLastUpdated()));
        }

        if (edit) {
            hubJRepo.saveAndFlush(hub);
        }

        response.put("status_desc", "edit_success");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }


}
