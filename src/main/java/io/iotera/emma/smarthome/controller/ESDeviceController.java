package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.preference.DevicePref;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.repository.ESDeviceRepository.ESDeviceJpaRepository;
import io.iotera.emma.smarthome.repository.ESRoomRepository;
import io.iotera.emma.smarthome.routine.RoutineManagerYoutube;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ESDeviceController extends ESBaseController {

    @Autowired
    ESDeviceRepository deviceRepository;

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    @Autowired
    ESApplicationInfoRepository applicationInfoRepository;

    @Autowired
    ESDeviceJpaRepository deviceJpaRepository;

    @Autowired
    CameraManager cameraManager;

    @Autowired
    RoutineManagerYoutube routineManagerYoutube;

    @Autowired
    ESRoomRepository roomRepository;

    @Autowired
    YoutubeService youtubeService;

    public ResponseEntity listAll(long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        Map<String, String> rooms = roomRepository.listRoomName(accountId);
        List<ESDevice> devices = deviceRepository.listByAccountId(accountId);

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

    protected ResponseEntity listRoom(String roomId, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        List<ESDevice> devices = deviceRepository.listByRoomId(roomId, accountId);
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
            deviceObject.put("room_name", device.getRoom().getName());
            deviceObject.put("parent", device.getParent());

            deviceArray.add(deviceObject);
        }

        response.set("devices", deviceArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity listCategory(int category, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ArrayNode deviceArray = Json.buildArrayNode();
        List<ESDevice> devices = deviceRepository.listByCategory(category, accountId);
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
            deviceObject.put("room_name", device.getRoom().getName());
            deviceObject.put("parent", device.getParent());

            deviceArray.add(deviceObject);
        }

        response.set("devices", deviceArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity create(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // CREATE
        String label = rget(body, "eslabel");
        String roomId = rget(body, "esroom");
        int category = rget(body, "escat", Integer.class);
        int type = rget(body, "estype", Integer.class);
        String info = get(body, "esinfo");

        // Check room
        ESRoom room = roomRepository.findByRoomId(roomId, accountId);
        if (room == null) {
            return okJsonFailed(-1, "room_not_found");
        }

        // Check device label
        if (!deviceRepository.findByLabel(label, accountId).isEmpty()) {
            return okJsonFailed(-2, "device_label_not_available");
        }

        ESDevice device;
        if (!DevicePref.isAppliance(category)) {

            String uid = rget(body, "esuid");
            String address = rget(body, "esaddress");

            if (!deviceRepository.findByUid(uid, accountId).isEmpty()) {
                return okJsonFailed(-3, "device_uid_not_available");
            }

            if (!deviceRepository.findByAddress(address, accountId).isEmpty()) {
                return okJsonFailed(-4, "device_address_not_available");
            }

            if (category == DevicePref.CAT_REMOTE) {

                device = new ESDevice(label, category, type, uid, address, info, false, 0,
                        room, roomId, accountId);

                deviceJpaRepository.saveAndFlush(device);

            } else if (category == DevicePref.CAT_CAMERA) {

                Tuple.T2<String, String> token = accountCameraRepository.getAccessTokenAndRefreshToken(accountId);
                if (token == null) {
                    return okJsonFailed(-20, "youtube_api_not_available");
                }

                Tuple.T2<String, String> youtubeClientApi = applicationInfoRepository.getClientIdAndClientSecret();
                if (youtubeClientApi == null) {
                    return internalServerError("internal_server_error");
                }

                String clientId = youtubeClientApi._1;
                String clientSecret = youtubeClientApi._2;

                device = new ESDevice(label, category, type, uid, address, info, false, 0,
                        room, roomId, accountId);
                deviceJpaRepository.save(device);
                String cameraId = device.getId();

                cameraManager.putSchedule(accountId, cameraId);


                ResponseEntity responseYoutubeKey = accountCameraRepository.YoutubeKey(accountId);
                ObjectNode objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
                System.out.println("OBJECT KEY : " + objectKey);
                int maxqueue = Integer.parseInt(objectKey.get("max_history").toString().replaceAll("[^\\w\\s]", ""));
                routineManagerYoutube.updateSchedule(device, accountId, objectKey, label, maxqueue);

                deviceJpaRepository.flush();

            } else {

                device = new ESDevice(label, category, type, uid, address, info, false, 0,
                        room, roomId, accountId);

                deviceJpaRepository.saveAndFlush(device);

            }

        } else {
            // Appliance
            String remoteId = rget(body, "esremote");

            // Check remote
            ESDevice remote = deviceRepository.findByDeviceId(remoteId, accountId);
            if (remote == null || remote.getCategory() != DevicePref.CAT_REMOTE) {
                return okJsonFailed(-11, "remote_not_found");
            }

            // Check type
            if (!deviceRepository.findByType(type, remoteId, accountId).isEmpty()) {
                return okJsonFailed(-12, "type_not_available");
            }

            device = ESDevice.buildAppliance(label, category, type, "", "", info, false, 0,
                    room, remoteId, roomId, accountId);

            deviceJpaRepository.saveAndFlush(device);
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity read(String deviceId, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESDevice device = deviceRepository.findByDeviceId(deviceId, accountId);
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

    protected ResponseEntity update(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // UPDATE
        boolean edit = false;
        String deviceId = rget(body, "esdevice");

        ESDevice device = deviceRepository.findByDeviceId(deviceId, accountId);
        if (device == null) {
            return okJsonFailed(-1, "device_not_found");
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());

        if (has(body, "esroom")) {
            String roomId = get(body, "esroom");

            if (!device.getRoomId().equals(roomId)) {
                // Check room
                ESRoom room = roomRepository.findByRoomId(roomId, accountId);
                if (room == null) {
                    return okJsonFailed(-2, "room_not_found");
                }

                String remoteId = null;
                if (DevicePref.isAppliance(device.getCategory())) {
                    remoteId = device.getParent().split("/")[2];
                }

                device.setRoom(room);
                device.setParent(ESDevice.parent(remoteId, roomId, accountId));
                edit = true;

                response.put("room_name", room.getName());
            }
        }

        if (has(body, "eslabel")) {
            String label = get(body, "eslabel");

            // Check device label
            if (!device.getLabel().equals(label)) {
                if (!deviceRepository.findByLabel(label, accountId).isEmpty()) {
                    return okJsonFailed(-3, "device_label_not_available");
                }
                device.setLabel(label);
                edit = true;
            }
            response.put("label", label);
        }

        if (has(body, "esaddress")) {
            String address = get(body, "esaddress");
            if (!device.getAddress().equals(address)) {
                device.setAddress(address);
                edit = true;
            }
            response.put("address", address);
        }

        if (has(body, "esinfo")) {
            String info = get(body, "esinfo");
            if (!device.getInfo().equals(info)) {
                device.setInfo(info);
                edit = true;
            }
            response.put("info", info);
        }

        if (edit) {
            deviceJpaRepository.saveAndFlush(device);
        }

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity delete(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // DELETE
        String deviceId = rget(body, "esdevice");

        ESDevice device = deviceRepository.findByDeviceId(deviceId, accountId);
        if (device == null) {
            return okJsonFailed(-1, "device_not_found");
        }

        response.put("id", device.getId());
        response.put("label", device.getLabel());

        Date now = new Date();
        if (device.getCategory() == DevicePref.CAT_REMOTE) {
            deviceRepository.deleteChild(now, deviceId, accountId);
        } else if (device.getCategory() == DevicePref.CAT_CAMERA) {
            // TODO Stop Camera
            // Get old and current broadcastID and make it complete
            ObjectNode info = Json.parseToObjectNode(device.getInfo());


            cameraManager.removeSchedule(accountId, deviceId);
        }

        device.setDeleted(true);
        device.setDeletedTime(now);

        deviceJpaRepository.saveAndFlush(device);

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
