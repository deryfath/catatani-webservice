package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.model.device.ESRoom;
import io.iotera.emma.smarthome.repository.ESRoomRepo;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Json;
import io.iotera.util.Random;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public class ESRoomController extends ESBaseController {

    @Autowired
    ESRoomRepo roomRepo;

    @Autowired
    ESRoomRepo.ESRoomJRepo roomJRepo;

    protected ResponseEntity listAll(long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        ArrayNode roomArray = Json.buildArrayNode();
        List<ESRoom> rooms = roomRepo.listByHubId(hubId);
        for (ESRoom room : rooms) {
            ObjectNode roomObject = Json.buildObjectNode();
            roomObject.put("id", room.getId());
            roomObject.put("name", room.getName());
            roomObject.put("category", room.getCategory());
            roomObject.put("picture", room.picturePath(getProperty("host.path.remote")));
            roomObject.put("picture_last_updated",
                    formatDate(room.getPictureLastUpdated()));
            roomObject.put("info", room.getInfo());
            roomObject.put("parent", room.getParent());

            roomArray.add(roomObject);
        }

        response.set("rooms", roomArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity create(ObjectNode body, ESHub hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // CREATE
        String name = rget(body, "esname");
        int category = rget(body, "escat", Integer.class);
        String info = get(body, "esinfo");

        // Check room name
        if (!roomRepo.findByName(name, hubId).isEmpty()) {
            return okJsonFailed(-2, "room_name_not_available");
        }

        ESRoom room = new ESRoom(name, category, info, hubId);
        roomJRepo.save(room);

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.hubPath(hubId, "room", room.getId());
            String attachment = getProperty("attachment.path");

            byte[] data = Base64.decodeBase64(picture);
            String filename = Random.alphaNumericLowerCase(8);
            ResourceUtility.save(data, attachment, path, filename);
            room.setPicture(path + "/" + filename);
            room.setPictureLastUpdated(new Date());
        }
        roomJRepo.saveAndFlush(room);

        response.put("id", room.getId());
        response.put("name", room.getName());
        response.put("category", room.getCategory());
        response.put("picture", room.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated",
                formatDate(room.getPictureLastUpdated()));
        response.put("info", room.getInfo());
        response.put("parent", room.getParent());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity read(String roomId, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        ESRoom room = roomRepo.findByRoomId(roomId, hubId);
        if (room == null) {
            return notFound("room (" + roomId + ") not found");
        }

        response.put("id", room.getId());
        response.put("name", room.getName());
        response.put("category", room.getCategory());
        response.put("picture", room.picturePath(getProperty("host.path.remote")));
        response.put("picture_last_updated",
                formatDate(room.getPictureLastUpdated()));
        response.put("info", room.getInfo());
        response.put("parent", room.getParent());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity update(ObjectNode body, ESHub hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // UPDATE
        boolean edit = false;
        String roomId = rget(body, "esroom");

        ESRoom room = roomRepo.findByRoomId(roomId, hubId);
        if (room == null) {
            return okJsonFailed(-1, "room_not_found");
        }

        response.put("id", room.getId());
        response.put("name", room.getName());

        if (has(body, "esname")) {
            String name = get(body, "esname");
            // Check room name
            if (!room.getName().equals(name)) {
                if (!roomRepo.findByName(name, hubId).isEmpty()) {
                    return okJsonFailed(-2, "room_name_not_available");
                }
                room.setName(name);
                edit = true;
            }
            response.put("name", room.getName());
        }

        if (has(body, "escat")) {
            int category = get(body, "escat", Integer.class);
            if (room.getCategory() != category) {
                room.setCategory(category);
                edit = true;
            }
            response.put("category", room.getCategory());
        }

        if (has(body, "esinfo")) {
            String info = get(body, "esinfo");
            if (!room.getInfo().equals(info)) {
                room.setInfo(info);
                edit = true;
            }
            response.put("info", room.getInfo());
        }

        if (has(body, "espic")) {
            String picture = get(body, "espic");
            String path = ResourceUtility.hubPath(hubId, "room", room.getId());
            String attachment = getProperty("attachment.path");

            // Delete current picture
            if (room.getPicture() != null) {
                String filename = ResourceUtility.filename(room.getPicture());
                ResourceUtility.delete(attachment, path, filename);
            }
            if (!picture.isEmpty()) {
                // Update
                byte[] data = Base64.decodeBase64(picture);
                String newFilename = Random.alphaNumericLowerCase(8);
                ResourceUtility.save(data, attachment, path, newFilename);
                room.setPicture(path + "/" + newFilename);
            } else {
                // Delete
                room.setPicture(null);
            }
            room.setPictureLastUpdated(new Date());
            edit = true;

            response.put("picture", room.picturePath(getProperty("host.path.remote")));
            response.put("picture_last_updated", formatDate(room.getPictureLastUpdated()));
        }

        if (edit) {
            roomJRepo.saveAndFlush(room);
        }

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity delete(ObjectNode body, ESHub hub, long hubId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // DELETE
        String roomId = rget(body, "esroom");

        ESRoom room = roomRepo.findByRoomId(roomId, hubId);
        if (room == null) {
            return okJsonFailed(-1, "room_not_found");
        }

        response.put("id", room.getId());
        response.put("name", room.getName());

        // Delete child
        Date now = new Date();
        roomRepo.deleteChild(now, roomId, hubId);

        // Delete picture
        String attachment = getProperty("attachment.path");
        String path = ResourceUtility.hubPath(hubId, "room", room.getId());
        String filename = room.getId();
        ResourceUtility.delete(attachment, path, filename);
        room.setPicture(null);

        room.setDeleted(true);
        room.setDeletedTime(now);

        roomJRepo.saveAndFlush(room);

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
