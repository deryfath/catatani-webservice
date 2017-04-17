package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.camera.CameraStartTask;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.mqtt.MqttPublishEvent;
import io.iotera.emma.smarthome.preference.CommandPref;
import io.iotera.emma.smarthome.preference.DevicePref;
import io.iotera.emma.smarthome.repository.*;
import io.iotera.emma.smarthome.util.PublishUtility;
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ESDeviceController extends ESBaseController implements ApplicationEventPublisherAware {

    @Autowired
    ESDeviceRepo deviceRepo;

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    @Autowired
    ESDeviceRepo.ESDeviceJRepo deviceJRepo;

    @Autowired
    CameraManager cameraManager;
    @Autowired
    ESRoomRepo roomRepo;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    PrologVideo prologVideo;

    @Autowired
    CameraStartTask cameraStartTask;

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

    private volatile ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("MASUK APPLICATION EVENT");
        this.applicationEventPublisher = applicationEventPublisher;

    }

    protected ResponseEntity listAll(long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        Map<String, String> rooms = roomRepo.listRoomName(hubId);
        List<ESDevice> devices = deviceRepo.listByHubId(hubId);

        for (String roomId : rooms.keySet()) {
            for (ESDevice device : devices) {
                String deviceRoomId = device.getRoomId();
                if (roomId.equals(deviceRoomId)) {
                    ObjectNode deviceObject = Json.buildObjectNode();

                    deviceObject.put("id", device.getId());
                    deviceObject.put("label", device.getLabel());
                    deviceObject.put("category", device.getCategory());
                    deviceObject.put("type", device.getType());
                    deviceObject.put("uid", device.getUid());
                    deviceObject.put("address", device.getAddress());
                    deviceObject.put("info", device.getInfo());
                    deviceObject.put("status_on", device.isOn());
                    deviceObject.put("status_state", device.getState());

                    String roomName = rooms.get(roomId);
                    deviceObject.put("room_name", roomName);
                    deviceObject.put("parent", device.getParent());

                    deviceArray.add(deviceObject);
                }
            }
        }

        response.set("devices", deviceArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity listRoom(String roomId, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        Map<String, String> rooms = roomRepo.listRoomName(hubId);
        List<ESDevice> devices = deviceRepo.listByRoomId(roomId, hubId);
        for (ESDevice device : devices) {
            ObjectNode deviceObject = Json.buildObjectNode();

            deviceObject.put("id", device.getId());
            deviceObject.put("label", device.getLabel());
            deviceObject.put("category", device.getCategory());
            deviceObject.put("type", device.getType());
            deviceObject.put("uid", device.getUid());
            deviceObject.put("address", device.getAddress());
            deviceObject.put("info", device.getInfo());
            deviceObject.put("status_on", device.isOn());
            deviceObject.put("status_state", device.getState());

            String roomName = rooms.get(roomId);
            deviceObject.put("room_name", roomName);
            deviceObject.put("parent", device.getParent());

            deviceArray.add(deviceObject);
        }

        response.set("devices", deviceArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity listCategory(int category, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        Map<String, String> rooms = roomRepo.listRoomName(hubId);
        List<ESDevice> devices = deviceRepo.listByCategory(category, hubId);
        for (ESDevice device : devices) {
            ObjectNode deviceObject = Json.buildObjectNode();

            deviceObject.put("id", device.getId());
            deviceObject.put("label", device.getLabel());
            deviceObject.put("category", device.getCategory());
            deviceObject.put("type", device.getType());
            deviceObject.put("uid", device.getUid());
            deviceObject.put("address", device.getAddress());
            deviceObject.put("info", device.getInfo());
            deviceObject.put("status_on", device.isOn());
            deviceObject.put("status_state", device.getState());

            String roomName = rooms.get(device.getRoomId());
            deviceObject.put("room_name", roomName);
            deviceObject.put("parent", device.getParent());

            deviceArray.add(deviceObject);
        }

        response.set("devices", deviceArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity create(ObjectNode body, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // CREATE
        String label = rget(body, "eslabel");
        String roomId = rget(body, "esroom");
        int category = rget(body, "escat", Integer.class);
        int type = rget(body, "estype", Integer.class);
        String state = get(body, "esstate");
        if (state == null) {
            state = "{\"st\", 0}";
        }
        String info = get(body, "esinfo");

        // Check room
        ESRoom room = roomRepo.findByRoomId(roomId, hubId);
        if (room == null) {
            return okJsonFailed(-1, "room_not_found");
        }

        // Check device label
        if (!deviceRepo.findByLabel(label, hubId).isEmpty()) {
            return okJsonFailed(-2, "device_label_not_available");
        }

        ESDevice device;
        if (!DevicePref.isAppliance(category)) {

            String uid = rget(body, "esuid");
            String address = rget(body, "esaddress");

            if (!deviceRepo.findByUid(uid, hubId).isEmpty()) {
                return okJsonFailed(-3, "device_uid_not_available");
            }

            if (category == DevicePref.CAT_REMOTE) {

                device = new ESDevice(label, category, type, uid, address, info, false, state,
                        roomId, hubId);

                deviceJRepo.saveAndFlush(device);

            } else if (category == DevicePref.CAT_CAMERA) {

                if (!deviceRepo.findByAddress(address, hubId).isEmpty()) {
                    return okJsonFailed(-4, "device_address_not_available");
                }

                Tuple.T2<String, String> token = hubCameraRepo.getAccessTokenAndRefreshToken(hubId);
                if (token == null) {
                    return okJsonFailed(-20, "youtube_api_not_available");
                }

                System.out.println("masuk client secret");

                Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
                if (youtubeClientApi == null) {
                    return internalServerError("internal_server_error");
                }

                String clientId = youtubeClientApi._1;
                String clientSecret = youtubeClientApi._2;

                device = new ESDevice(label, category, type, uid, address, info, false, null,
                        roomId, hubId);
                deviceJRepo.save(device);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                System.out.println(dateFormat.format(date).toString());

                //round time for prolog
                Date dateHoursRound = cameraStartTask.toNearestWholeHour(date);
                System.out.println(dateFormat.format(dateHoursRound).toString());
                ResponseEntity responseEntityStream = prologVideo.runVideoProlog(label + " " + dateFormat.format(dateHoursRound).toString(), hubId);
                ObjectNode objectEntityStream = Json.parseToObjectNode(responseEntityStream.getBody().toString());

                System.out.println(objectEntityStream);
                if (objectEntityStream.get("status_code") != null) {
                    if (objectEntityStream.get("status_code").asInt() != 200) {
                        return okJsonFailed(objectEntityStream.get("status_code").asInt(), objectEntityStream.get("status_desc").textValue());
                    }
                }

                cameraManager.putSchedule(hubId, device, label, objectEntityStream);

//                routineManagerYoutube.updateSchedule(device, hubId, objectKey, label);

                deviceJRepo.flush();

            } else {

                device = new ESDevice(label, category, type, uid, address, info, false, state,
                        roomId, hubId);

                deviceJRepo.saveAndFlush(device);

            }

        } else {
            // Appliance
            String remoteId = rget(body, "esremote");

            // Check remote
            ESDevice remote = deviceRepo.findByDeviceId(remoteId, hubId);
            if (remote == null || remote.getCategory() != DevicePref.CAT_REMOTE) {
                return okJsonFailed(-11, "remote_not_found");
            }

            // Check type
            if (!deviceRepo.findByType(type, remoteId, hubId).isEmpty()) {
                return okJsonFailed(-12, "type_not_available");
            }

            device = ESDevice.buildAppliance(label, category, type, "", "", info, false, state,
                    remoteId, roomId, hubId);

            deviceJRepo.saveAndFlush(device);
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());
        response.put("category", device.getCategory());
        response.put("type", device.getType());
        response.put("uid", device.getUid());
        response.put("address", device.getAddress());
        response.put("status_on", device.isOn());
        response.put("status_state", device.getState());
        response.put("info", device.getInfo());
        response.put("parent", device.getParent());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity read(String deviceId, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESDevice device = deviceRepo.findByDeviceId(deviceId, hubId);
        if (device == null) {
            return notFound("device (" + deviceId + ") not found");
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());
        response.put("category", device.getCategory());
        response.put("type", device.getType());
        response.put("uid", device.getUid());
        response.put("address", device.getAddress());
        response.put("status_on", device.isOn());
        response.put("status_state", device.getState());
        response.put("info", device.getInfo());
        response.put("parent", device.getParent());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity update(ObjectNode body, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // UPDATE
        boolean edit = false;
        String deviceId = rget(body, "esdevice");

        ESDevice device = deviceRepo.findByDeviceId(deviceId, hubId);
        if (device == null) {
            return okJsonFailed(-1, "device_not_found");
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());

        if (has(body, "esroom")) {
            String roomId = get(body, "esroom");

            if (!device.getRoomId().equals(roomId)) {
                // Check room
                ESRoom room = roomRepo.findByRoomId(roomId, hubId);
                if (room == null) {
                    return okJsonFailed(-2, "room_not_found");
                }

                String remoteId = null;
                if (DevicePref.isAppliance(device.getCategory())) {
                    remoteId = device.getParent().split("/")[2];
                }

                device.setParent(ESDevice.parent(remoteId, roomId, hubId));
                edit = true;

                response.put("room_name", room.getName());
            }
        }

        if (has(body, "eslabel")) {
            String label = get(body, "eslabel");

            // Check device label
            if (!device.getLabel().equals(label)) {
                if (!deviceRepo.findByLabel(label, hubId).isEmpty()) {
                    return okJsonFailed(-3, "device_label_not_available");
                }
                device.setLabel(label);
                edit = true;
            }
            response.put("label", device.getLabel());
        }

        if (has(body, "esaddress")) {
            String address = get(body, "esaddress");
            if (!device.getAddress().equals(address)) {
                device.setAddress(address);
                edit = true;
            }
            response.put("address", device.getAddress());
        }

        if (has(body, "esinfo")) {
            String info = get(body, "esinfo");
            if (!device.getInfo().equals(info)) {
                device.setInfo(info);
                edit = true;
            }
            response.put("info", device.getInfo());
        }

        if (edit) {
            deviceJRepo.saveAndFlush(device);
        }

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity delete(ObjectNode body, long hubId) {

        System.out.println("MASUK DELETE");

        // Response
        ObjectNode response = Json.buildObjectNode();

        // DELETE
        String deviceId = rget(body, "esdevice");

        ESDevice device = deviceRepo.findByDeviceId(deviceId, hubId);
        if (device == null) {
            return okJsonFailed(-1, "device_not_found");
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());

        Date now = new Date();
        if (device.getCategory() == DevicePref.CAT_REMOTE) {
            deviceRepo.deleteChild(now, deviceId, hubId);

        } else if (device.getCategory() == DevicePref.CAT_CAMERA) {
            // TODO Stop Camera
            // Get old and current broadcastID and make it complete
            ObjectNode info = Json.parseToObjectNode(device.getInfo());

            cameraHistoryRepo.updateDeleteStatus(deviceId,hubId);
            deviceRepo.deleteChild(now, deviceId, hubId);

            cameraManager.removeSchedule(hubId, deviceId);

            Message<String> message2;


            //MQTT SEND MESSAGE NULL CAMERA START
            message2 = MessageBuilder
                    .withPayload("")
                    .setHeader(MqttHeaders.TOPIC,
                            PublishUtility.topicHub(hubId, CommandPref.CAMERA_START, deviceId))
                    .build();

            if (applicationEventPublisher != null && message2 != null) {
                applicationEventPublisher.publishEvent(new MqttPublishEvent(this, CommandPref.CAMERA_START, message2));
            } else {
                System.out.println("MQTT NULL");
            }

        }

        device.setDeleted(true);
        device.setDeletedTime(now);

        deviceJRepo.saveAndFlush(device);

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }


}
