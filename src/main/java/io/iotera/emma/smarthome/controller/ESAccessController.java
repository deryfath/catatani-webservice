package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Base64;
import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.preference.PermissionPref;
import io.iotera.emma.smarthome.repository.ESAccessRepo;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
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
import java.util.List;

public class ESAccessController extends ESBaseController implements ApplicationEventPublisherAware {

    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;

    @Autowired
    ESAccessRepo accessRepo;

    @Autowired
    ESAccessRepo.ESAccessJRepo accessJRepo;

    @Autowired
    ESHubRepo hubRepo;

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    protected ResponseEntity connect(ObjectNode body, ESAccount client, long clientId) {

        System.out.println("MASUK HOME CONNECT");

        // Request Body
        String rtoken = rget(body, "esrtoken");

        System.out.println(rtoken);

        ESHub hub = hubJRepo.findByRegistrationTokenAndHubActiveFalseAndDeactivateFalse(rtoken);
        if (hub == null) {
            return okJsonFailed(-1, "token_not_valid");
        }
        long hubId = hub.getId();
        String suid = hub.getSuid();

        Date now = new Date();
        hub.setHubActive(true);
        hub.setHubActiveTime(now);
        hub.generateHubToken();
        hub.setClient(client);
        hub.setDeactivate(false);
        hub.setDeactivateTime(null);

        if (has(body, "esname")) {
            String name = get(body, "esname");
            if (!hub.getName().equals(name)) {
                hub.setName(name);
            }
        }

        if (has(body, "esaddress")) {
            String address = get(body, "esaddress");
            if (!hub.getAddress().equals(address)) {
                hub.setAddress(address);
            }
        }

        if (has(body, "eslat")) {
            String latitude = get(body, "eslat");
            if (!hub.getLatitude().equals(latitude)) {
                hub.setLatitude(latitude);
            }
        }

        if (has(body, "eslong")) {
            String longitude = get(body, "eslong");
            if (!hub.getLongitude().equals(longitude)) {
                hub.setLongitude(longitude);
            }
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
        }

        hubJRepo.saveAndFlush(hub);

        if (applicationEventPublisher != null) {

            ObjectNode payload = Json.buildObjectNode();
            payload.put("htoken", hub.getHubToken());

            Message<String> sendMessage = MessageBuilder
                    .withPayload(payload.toString())
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topicHubReg(suid, CommandPref.LOGIN))
                    .setHeader(MqttHeaders.RETAINED, true)
                    .build();

            applicationEventPublisher.publishEvent(new MqttPublishEvent(
                    this, CommandPref.FORCE_HOMEKICK, sendMessage));
        }

        // Result
        return okJsonSuccess("connect_success");
    }

    protected ResponseEntity listHome(ESAccount client, long clientId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESAccess access;

        ArrayNode hubArray = Json.buildArrayNode();
        ObjectNode hubObject;

        // Add default access
        List<ESHub> hubs = hubRepo.listHubByClientId(clientId);
        for (ESHub hub : hubs) {
            long hubId = hub.getId();
            access = ESAccess.buildOwnerAccess(hubId, clientId, hub.getHubActiveTime());

            hubObject = Json.buildObjectNode();
            hubObject.put("id", hub.getId());
            hubObject.put("name", hub.getName());
            hubObject.put("address", hub.getAddress());
            hubObject.put("picture", hub.picturePath(getProperty("host.path.remote")));
            hubObject.put("picture_last_updated", formatDate(
                    hub.getPictureLastUpdated()));
            hubObject.put("latitude", hub.getLatitude());
            hubObject.put("longitude", hub.getLongitude());
            hubObject.put("atoken", access.getAccessToken());
            hubObject.put("permission", access.permission());
            hubObject.put("added_time", formatDate(access.getAddedTime()));
            hubObject.put("client_id", clientId);

            hubArray.add(hubObject);
        }

        List<Object[]> accessList = accessRepo.listHubByClientId(clientId);
        for (Object[] objects : accessList) {
            ESHub hub = (ESHub) objects[0];
            access = (ESAccess) objects[1];

            hubObject = Json.buildObjectNode();
            hubObject.put("id", hub.getId());
            hubObject.put("name", hub.getName());
            hubObject.put("address", hub.getAddress());
            hubObject.put("picture", hub.picturePath(getProperty("host.path.remote")));
            hubObject.put("picture_last_updated", formatDate(
                    hub.getPictureLastUpdated()));
            hubObject.put("latitude", hub.getLatitude());
            hubObject.put("longitude", hub.getLongitude());
            hubObject.put("atoken", access.getAccessToken());
            hubObject.put("permission", access.permission());
            hubObject.put("added_time", formatDate(access.getAddedTime()));
            hubObject.put("client_id", clientId);

            hubArray.add(hubObject);
        }

        // Result
        response.set("hubs", hubArray);
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity getAccess(ESAccount client, long clientId, ESHub hub, long hubId) {

        ESAccess access = accessRepo.findAccessByHubAndClient(hub, hubId, client, clientId);
        if (access == null) {
            return okJsonFailed(-1, "access_not_available");
        }

        ObjectNode response = Json.buildObjectNode();
        response.put("atoken", access.getAccessToken());
        response.put("permission", access.permission());
        response.put("added_time", formatDate(access.getAddedTime()));
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity listMember(ESHub hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESAccess access;
        ESAccount client;

        ArrayNode clientArray = Json.buildArrayNode();
        ObjectNode clientObject;

        // Add default access
        client = hub.getClient();
        access = ESAccess.buildOwnerAccess(hubId, client.getId(), hub.getHubActiveTime());

        clientObject = Json.buildObjectNode();
        clientObject.put("id", client.getId());
        clientObject.put("username", client.getUsername());
        clientObject.put("email", client.getEmail());
        clientObject.put("first_name", client.getFirstName());
        clientObject.put("last_name", client.getLastName());
        clientObject.put("picture", client.picturePath(getProperty("host.path.remote")));
        clientObject.put("picture_last_updated", formatDate(client.getPictureLastUpdated()));
        clientObject.put("permission", access.permission());
        clientObject.put("added_time", formatDate(access.getAddedTime()));
        clientObject.put("account_id", access.getHubId());

        clientArray.add(clientObject);

        List<Object[]> clients = accessRepo.listClientByHubId(hubId);
        for (Object[] objects : clients) {
            client = (ESAccount) objects[0];
            access = (ESAccess) objects[1];

            clientObject = Json.buildObjectNode();
            clientObject.put("id", client.getId());
            clientObject.put("username", client.getUsername());
            clientObject.put("email", client.getEmail());
            clientObject.put("first_name", client.getFirstName());
            clientObject.put("last_name", client.getLastName());
            clientObject.put("picture", client.picturePath(getProperty("host.path.remote")));
            clientObject.put("picture_last_updated", formatDate(
                    client.getPictureLastUpdated()));
            clientObject.put("permission", access.permission());
            clientObject.put("added_time", formatDate(access.getAddedTime()));
            clientObject.put("account_id", access.getHubId());

            clientArray.add(clientObject);
        }

        // Result
        response.set("clients", clientArray);
        response.put("status_desc", "");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity createMember(ObjectNode body, ESHub hub, long hubId) {

        String clientIdString = get(body, "esclient");
        String username = get(body, "esuser");

        long clientId;
        ESAccount client;
        if (clientIdString != null) {
            try {
                clientId = Long.parseLong(clientIdString);
            } catch (NumberFormatException e) {
                return badRequest("wrong client id format");
            }

            client = accountJRepo.findByIdAndDeactivateFalse(clientId);
            if (client == null) {
                return okJsonFailed(-1, "client_not_found");
            }
        } else if (username != null) {
            client = accountJRepo.findByUsernameAndDeactivateFalse(username);
            if (client == null) {
                return okJsonFailed(-2, "client_not_found");
            }
            clientId = client.getId();
        } else {
            return badRequest("client required");
        }

        if (clientId == hubId) {
            return okJsonFailed(-4, "forbidden");
        }

        ESAccess access = accessRepo.findAccessByHubAndClient(hub, hubId, client, clientId);
        if (access != null) {
            return okJsonFailed(-3, "access_not_available");
        }
        access = new ESAccess(hubId, client.getId());
        accessJRepo.saveAndFlush(access);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("username", client.getUsername());
        response.put("email", client.getEmail());
        response.put("first_name", client.getFirstName());
        response.put("last_name", client.getLastName());
        response.put("picture", client.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(client.getPictureLastUpdated()));
        response.put("permission", access.permission());
        response.put("added_time", formatDate(access.getAddedTime()));
        response.put("account_id", access.getHubId());
        response.put("status_desc", "client_added");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity updateAdmin(ObjectNode body, ESHub hub, long hubId) {

        String clientIdString = get(body, "esclient");
        String username = get(body, "esuser");
        boolean admin = rget(body, "esadmin", Boolean.class);

        long clientId;
        ESAccount client;
        if (clientIdString != null) {
            try {
                clientId = Long.parseLong(clientIdString);
            } catch (NumberFormatException e) {
                return badRequest("wrong client id format");
            }

            client = accountJRepo.findByIdAndDeactivateFalse(clientId);
            if (client == null) {
                return okJsonFailed(-1, "client_not_found");
            }
        } else if (username != null) {
            client = accountJRepo.findByUsernameAndDeactivateFalse(username);
            if (client == null) {
                return okJsonFailed(-2, "client_not_found");
            }
            clientId = client.getId();
        } else {
            return badRequest("client required");
        }

        if (clientId == hubId) {
            return okJsonFailed(-4, "forbidden");
        }

        ESAccess access = accessRepo.findAccessByHubAndClient(hub, hubId, client, clientId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_found");
        }

        if (admin) {
            if (!PermissionPref.isOwnerOrAdmin(access.permission())) {
                access.setPermission("[admin]");
                accessJRepo.saveAndFlush(access);
            }
        } else {
            if (!PermissionPref.isOwner(access.permission())) {
                access.setPermission("");
                accessJRepo.saveAndFlush(access);
            }
        }

        // Response
        ObjectNode response = Json.buildObjectNode();

        response.put("id", client.getId());
        response.put("username", client.getUsername());
        response.put("email", client.getEmail());
        response.put("permission", access.permission());
        response.put("status_desc", "edit_permission_success");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity deleteMember(ObjectNode body, ESHub hub, long hubId) {

        String clientIdString = get(body, "esclient");
        long clientId;
        String username = get(body, "esuser");

        ESAccount client;
        if (clientIdString != null) {
            clientId = Long.parseLong(clientIdString);
            client = accountJRepo.findByIdAndDeactivateFalse(clientId);
            if (client == null) {
                return okJsonFailed(-1, "client_not_found");
            }
        } else if (username != null) {
            client = accountJRepo.findByUsernameAndDeactivateFalse(username);
            if (client == null) {
                return okJsonFailed(-2, "client_not_found");
            }
            clientId = client.getId();
        } else {
            return badRequest("client required");
        }

        if (clientId == hubId) {
            return okJsonFailed(-4, "forbidden");
        }

        ESAccess access = accessRepo.findAccessByHubAndClient(hub, hubId, client, clientId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_found");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("username", client.getUsername());
        response.put("email", client.getEmail());

        access.setDeleted(true);
        access.setDeletedTime(new Date());

        accessJRepo.saveAndFlush(access);

        response.put("status_desc", "client_deleted");
        response.put("status_code", 0);
        response.put("status", "success");

        if (applicationEventPublisher != null) {
            Message<String> sendMessage = MessageBuilder
                    .withPayload("")
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topicClientHub(
                                    clientId, CommandPref.FORCE_HOMEKICK, null))
                    .build();

            applicationEventPublisher.publishEvent(new MqttPublishEvent(
                    this, CommandPref.FORCE_HOMEKICK, sendMessage));
        }

        // Result
        return okJson(response);
    }


}
