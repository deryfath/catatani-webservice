package io.iotera.emma.smarthome.model.device;

import io.iotera.emma.model.device.EDevice;
import io.iotera.emma.smarthome.model.camera.ESCameraHistory;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "device_tbl")
public class ESDevice extends EDevice {

    @Column(name = "__parent__", nullable = false)
    protected String parent;

    ////////////
    // Column //
    ////////////

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    protected ESRoom room;

    /////////////////
    // Constructor //
    /////////////////

    protected ESDevice() {
    }

    public ESDevice(String label, int category, int type, String uid, String address, String info,
                    boolean on, int state, ESRoom room,
                    String roomId, long accountId) {
        super(label, category, type, uid, address, info, on, state);
        this.room = room;
        this.parent = parent(null, roomId, accountId);
    }

    public ESDevice(String parent, ESRoom room) {
        this.parent = parent;
        this.room = room;
    }

    public ESDevice(String label, int category, int type, String uid, String address, String info, boolean on, int state, String parent, ESRoom room) {
        super(label, category, type, uid, address, info, on, state);
        this.parent = parent;
        this.room = room;
    }

    ///////////////
    // Appliance //
    public static ESDevice buildAppliance(String label, int category, int type, String uid, String address,
                                          String info, boolean on, int state,
                                          ESRoom room, String remoteId, String roomId, long accountId) {
        ESDevice appliance = new ESDevice(label, category, type, uid, address, info, on, state, room, roomId, accountId);
        appliance.parent = parent(remoteId, roomId, accountId);
        return appliance;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }


    public ESRoom getRoom() {
        return room;
    }

    public void setRoom(ESRoom room) {
        this.room = room;
    }

    @Transient
    public String getRoomId() {
        return parent.split("/")[1];
    }

    ////////////
    // Method //
    ////////////

    public static String parent(String deviceId, String roomId, long accountId) {
        StringBuilder pBuilder = new StringBuilder();
        pBuilder.append(accountId);
        pBuilder.append('/');
        roomId = (roomId != null) ? roomId : "%";
        pBuilder.append(roomId);
        if (roomId.equals("%")) {
            if (deviceId != null) {
                pBuilder.append('/');
            }
        } else {
            pBuilder.append('/');
        }
        if (deviceId != null) {
            pBuilder.append(deviceId);
            if (!deviceId.equals("%")) {
                pBuilder.append('/');
            }
        }

        return pBuilder.toString();
    }

}
