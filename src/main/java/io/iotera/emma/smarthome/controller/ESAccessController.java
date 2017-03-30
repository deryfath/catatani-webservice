package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountClient;
import io.iotera.emma.smarthome.model.account.ESAccountHub;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.repository.ESAccessRepo;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.util.Json;
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

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    protected ResponseEntity listHome(ESAccount client, long clientId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESAccountHub accountHub;
        ESAccess access;

        ArrayNode accountArray = Json.buildArrayNode();
        ObjectNode accountObject;

        if (client.isHubActive()) {
            // Add default access
            accountHub = client.getAccountHub();
            access = ESAccess.buildDefaultAccess(clientId, client.getHubActiveTime());

            accountObject = Json.buildObjectNode();
            accountObject.put("id", client.getId());
            accountObject.put("name", accountHub.getName());
            accountObject.put("address", accountHub.getAddress());
            accountObject.put("picture", accountHub.picturePath(getProperty("host.path.remote")));
            accountObject.put("picture_last_updated", formatDate(
                    accountHub.getPictureLastUpdated()));
            accountObject.put("latitude", accountHub.getLatitude());
            accountObject.put("longitude", accountHub.getLongitude());
            accountObject.put("atoken", access.getAccessToken());
            accountObject.put("admin", access.isAdmin());
            accountObject.put("added_time", formatDate(access.getAddedTime()));
            accountObject.put("client_id", clientId);

            accountArray.add(accountObject);
        }

        List<Object[]> hubs = accessRepo.listHubByClientId(clientId);
        for (Object[] objects : hubs) {
            ESAccount hub = (ESAccount) objects[0];
            accountHub = hub.getAccountHub();
            access = (ESAccess) objects[1];

            accountObject = Json.buildObjectNode();
            accountObject.put("id", hub.getId());
            accountObject.put("name", accountHub.getName());
            accountObject.put("address", accountHub.getAddress());
            accountObject.put("picture", accountHub.picturePath(getProperty("host.path.remote")));
            accountObject.put("picture_last_updated", formatDate(
                    accountHub.getPictureLastUpdated()));
            accountObject.put("latitude", accountHub.getLatitude());
            accountObject.put("longitude", accountHub.getLongitude());
            accountObject.put("atoken", access.getAccessToken());
            accountObject.put("admin", access.isAdmin());
            accountObject.put("added_time", formatDate(access.getAddedTime()));
            accountObject.put("client_id", clientId);

            accountArray.add(accountObject);
        }

        // Result
        response.set("accounts", accountArray);
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity getAccess(ESAccount client, long clientId, long hubId) {

        ESAccess access = accessRepo.findAccessByClientIdHubId(client, clientId, hubId);
        if (access == null) {
            return okJsonFailed(-1, "access_not_available");
        }

        ObjectNode response = Json.buildObjectNode();
        response.put("atoken", access.getAccessToken());
        response.put("admin", access.isAdmin());
        response.put("added_time", formatDate(access.getAddedTime()));
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity connect(ObjectNode body, ESAccount client, long clientId) {

        String hubIdString = rget(body, "eshub");
        long hubId;
        try {
            hubId = Long.parseLong(hubIdString);
        } catch (NumberFormatException e) {
            return badRequest("wrong client id format");
        }

        ESAccount hub = accountJRepo.findByIdAndDeactivateFalse(hubId);
        if (hub == null) {
            return okJsonFailed(-1, "account_not_found");
        }
        if (!hub.isHubActive()) {
            return okJsonFailed(-2, "hub_not_active");
        }

        if (clientId == hubId) {
            return okJsonFailed(-4, "forbidden");
        }

        ESAccess access = accessRepo.findAccessByClientIdHubId(client, clientId, hubId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_available");
        }

        access = new ESAccess(hub.getId(), clientId);
        accessJRepo.saveAndFlush(access);

        // Result
        return okJsonSuccess("connect_success");
    }

    protected ResponseEntity listMember(ESAccount hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESAccess access;
        ESAccountClient accountClient;

        ArrayNode accountArray = Json.buildArrayNode();
        ObjectNode accountObject;

        // Add default access
        accountClient = hub.getAccountClient();
        access = ESAccess.buildDefaultAccess(hubId, hub.getHubActiveTime());

        accountObject = Json.buildObjectNode();
        accountObject.put("id", hub.getId());
        accountObject.put("email", hub.getEmail());
        accountObject.put("username", hub.getUsername());
        accountObject.put("first_name", accountClient.getFirstName());
        accountObject.put("last_name", accountClient.getLastName());
        accountObject.put("picture", accountClient.picturePath(getProperty("host.path.remote")));
        accountObject.put("picture_last_updated", formatDate(accountClient.getPictureLastUpdated()));
        accountObject.put("admin", access.isAdmin());
        accountObject.put("added_time", formatDate(access.getAddedTime()));
        accountObject.put("account_id", access.getHubId());

        accountArray.add(accountObject);

        List<Object[]> clients = accessRepo.listClientByHubId(hubId);
        for (Object[] objects : clients) {
            ESAccount client = (ESAccount) objects[0];
            accountClient = client.getAccountClient();
            access = (ESAccess) objects[1];

            accountObject = Json.buildObjectNode();
            accountObject.put("id", client.getId());
            accountObject.put("email", client.getEmail());
            accountObject.put("username", client.getUsername());
            accountObject.put("first_name", accountClient.getFirstName());
            accountObject.put("last_name", accountClient.getLastName());
            accountObject.put("picture", accountClient.picturePath(getProperty("host.path.remote")));
            accountObject.put("picture_last_updated", formatDate(
                    accountClient.getPictureLastUpdated()));
            accountObject.put("admin", access.isAdmin());
            accountObject.put("added_time", formatDate(access.getAddedTime()));
            accountObject.put("account_id", access.getHubId());

            accountArray.add(accountObject);
        }

        // Result
        response.set("clients", accountArray);
        response.put("status_desc", "");
        response.put("status_code", 0);
        response.put("status", "success");

        return okJson(response);
    }

    protected ResponseEntity createMember(ObjectNode body, long hubId) {

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

        ESAccess access = accessRepo.findAccessByClientIdHubId(client, clientId, hubId);
        if (access != null) {
            return okJsonFailed(-3, "access_not_available");
        }
        access = new ESAccess(hubId, client.getId());
        accessJRepo.saveAndFlush(access);

        ESAccountClient accountClient = client.getAccountClient();

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("email", client.getEmail());
        response.put("username", client.getUsername());
        response.put("first_name", accountClient.getFirstName());
        response.put("last_name", accountClient.getLastName());
        response.put("picture", accountClient.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated", formatDate(accountClient.getPictureLastUpdated()));
        response.put("admin", access.isAdmin());
        response.put("added_time", formatDate(access.getAddedTime()));
        response.put("status_desc", "client_added");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity updateAdmin(ObjectNode body, long hubId) {

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

        ESAccess access = accessRepo.findAccessByClientIdHubId(client, clientId, hubId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_found");
        }

        if (access.isAdmin() != admin) {
            access.setPermission(admin ? "[admin]" : "");
            accessJRepo.saveAndFlush(access);
        }

        // Response
        ObjectNode response = Json.buildObjectNode();

        response.put("id", client.getId());
        response.put("email", client.getEmail());
        response.put("username", client.getUsername());
        response.put("admin", access.isAdmin());
        response.put("status_desc", "client_is_" + (admin ? "" : "not_") + "admin");
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity deleteMember(ObjectNode body, long hubId) {

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

        ESAccess access = accessRepo.findAccessByClientIdHubId(client, clientId, hubId);
        if (access == null) {
            return okJsonFailed(-3, "access_not_found");
        }

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("id", client.getId());
        response.put("email", client.getEmail());
        response.put("username", client.getUsername());

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
