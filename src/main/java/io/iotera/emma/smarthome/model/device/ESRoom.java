package io.iotera.emma.smarthome.model.device;

import io.iotera.emma.model.device.ERoom;
import io.iotera.emma.smarthome.util.ResourceUtility;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ESRoom.NAME)
public class ESRoom extends ERoom {

    public static final String NAME = "v2_room_tbl";

    @Column(name = "__parent__", nullable = false)
    protected String parent;

    /////////////////
    // Constructor //
    /////////////////

    protected ESRoom() {
    }

    public ESRoom(String name, int category, String info, long accountId) {
        super(name, category, info);
        this.parent = parent(accountId);

    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public static String parent(long accountId) {
        return accountId + "/";
    }

    public String getParent() {
        return parent;
    }

    ////////////
    // Method //
    ////////////

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String picturePath(String hostPath) {
        if (picture == null) {
            return "";
        }
        return ResourceUtility.resourceImagePath(hostPath, picture);
    }

}

