package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.camera.CameraManager;
import io.iotera.emma.smarthome.camera.CameraRemoveTaskItem;
import io.iotera.emma.smarthome.camera.CameraStartTaskItem;
import io.iotera.emma.smarthome.model.account.ESHubCamera;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.preference.DevicePref;
import io.iotera.emma.smarthome.repository.*;
import io.iotera.emma.smarthome.youtube.PrologVideo;
import io.iotera.emma.smarthome.youtube.YoutubeItem;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ESDeviceController extends ESBaseController {

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
    PrologVideo prologVideo;

    @Autowired
    ESCameraHistoryRepo cameraHistoryRepo;

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

                ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
                if (hubCamera == null) {
                    return okJsonFailed(-5, "youtube_api_not_available");
                }

                String accessToken = hubCamera.getAccessToken();
                String refreshToken = hubCamera.getRefreshToken();
                int maxQueue = hubCamera.getMaxHistory();

                Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
                if (youtubeClientApi == null) {
                    return internalServerError("internal_server_error");


                }

                String clientId = youtubeClientApi._1;
                String clientSecret = youtubeClientApi._2;

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date time = calendar.getTime();

                String title = label + " " + formatDate(time);

                Tuple.T2<Integer, YoutubeItem> prologResult = prologVideo.runVideoProlog(title, hubId);
                System.out.println("PROLOG RESULT : "+prologResult._1);
                if (prologResult._1 != 0) {
                    return okJsonFailed(-6, "prolog_video_failed");
                }

                device = new ESDevice(label, category, type, uid, address, info, false, null,
                        roomId, hubId);
                deviceJRepo.save(device);
                String cameraId = device.getId();
                String infoString = device.getInfo();

                YoutubeItem youtubeItem = prologResult._2;
                youtubeItem.setTime(time);

                cameraManager.putSchedule(hubId, cameraId,
                        new CameraStartTaskItem(title, roomId, clientId, clientSecret, accessToken,
                                refreshToken, maxQueue, youtubeItem)
                );

                ObjectNode newInfo = Json.appendObjectNodeString(infoString, youtubeItem.getInfo());
                device.setInfo(Json.toStringIgnoreNull(newInfo));

                deviceJRepo.saveAndFlush(device);

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

                //UPDATE AND REPLACE CAMERA HISTORY
                cameraHistoryRepo.updateAndReplaceLabel(device.getId(), hubId, label, device.getLabel());

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

        // Delete device on database
        Date now = new Date();
        device.setDeleted(true);
        device.setDeletedTime(now);
        deviceJRepo.saveAndFlush(device);

        if (device.getCategory() == DevicePref.CAT_REMOTE) {
            deviceRepo.deleteChild(now, deviceId, hubId);

        } else if (device.getCategory() == DevicePref.CAT_CAMERA) {

            // Obtain Client Id and Client secret
            Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
            if (youtubeClientApi == null) {
                return internalServerError("");
            }

            String clientId = youtubeClientApi._1;
            String clientSecret = youtubeClientApi._2;

            // Obtain Access token and Refresh token
            ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
            if (hubCamera == null) {
                return okJsonFailed(-2, "device_not_found");
            }

            String accessToken = hubCamera.getAccessToken();
            String refreshToken = hubCamera.getRefreshToken();

            cameraHistoryRepo.updateDeleteStatus(now, deviceId, hubId);
            cameraManager.removeSchedule(hubId, deviceId,
                    new CameraRemoveTaskItem(now, clientId, clientSecret, accessToken, refreshToken));
        }


        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }


}
