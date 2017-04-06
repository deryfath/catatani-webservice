package io.iotera.emma.smarthome.model.device;

import io.iotera.emma.model.device.EDevice;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = ESDevice.NAME)
public class ESDevice extends EDevice {

    public static final String NAME = "v2_device_tbl";

    @Column(name = "__parent__", nullable = false)
    protected String parent;

    /////////////////
    // Constructor //
    /////////////////

    protected ESDevice() {
    }

    public ESDevice(String label, int category, int type, String uid, String address, String info,
                    boolean on, String state,
                    String roomId, long hubId) {
        super(label, category, type, uid, address, info, on, state);
        this.parent = parent(null, roomId, hubId);
    }

    public ESDevice(String parent) {
        this.parent = parent;
    }

    public ESDevice(String label, int category, int type, String uid, String address, String info,
                    boolean on, String state, String parent) {
        super(label, category, type, uid, address, info, on, state);
        this.parent = parent;
    }

    ///////////////
    // Appliance //
    public static ESDevice buildAppliance(String label, int category, int type, String uid, String address, String info,
                                          boolean on, String state,
                                          String remoteId, String roomId, long hubId) {
        ESDevice appliance = new ESDevice(label, category, type, uid, address, info, on, state, roomId, hubId);
        appliance.parent = parent(remoteId, roomId, hubId);
        return appliance;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public static String parent(String deviceId, String roomId, long hubId) {
        StringBuilder pBuilder = new StringBuilder();
        pBuilder.append(hubId);
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    ////////////
    // Method //
    ////////////

    @Transient
    public String getRoomId() {
        return parent.split("/")[1];
    }

}
