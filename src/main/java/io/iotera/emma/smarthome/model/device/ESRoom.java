package io.iotera.emma.smarthome.model.device;

import io.iotera.emma.model.device.ERoom;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.utility.ResourceUtility;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "room_tbl")
public class ESRoom extends ERoom {

    @Column(name = "__parent__", nullable = false)
    protected String parent;

    ////////////
    // Column //
    ////////////

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    public List<ESDevice> devices;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    protected ESRoom() {
    }

    public ESRoom(String name, int category, String info, ESAccount account, long accountId) {
        super(name, category, info);
        this.account = account;
        this.parent = parent(accountId);

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

    ////////////
    // Method //
    ////////////

    public String picturePath(String hostPath) {
        if (picture == null) {
            return "";
        }
        return ResourceUtility.resourceImagePath(hostPath, picture);
    }

    public static String parent(long accountId) {
        return accountId + "/";
    }

}
