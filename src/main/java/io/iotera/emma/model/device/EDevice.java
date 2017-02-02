package io.iotera.emma.model.device;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public class EDevice {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(nullable = false)
    protected String label;

    @Column(nullable = false)
    protected int category;

    @Column(nullable = false)
    protected int type;

    @Column(nullable = false)
    protected String uid;

    @Column
    protected String address;

    @Column(length = 1023)
    protected String info;

    ////////////
    // Status //
    @Column(name = "__status_on__", nullable = false)
    protected boolean on;

    @Column(name = "__status_state__", nullable = false)
    protected int state;

    ///////////
    // Order //
    @Column(name = "__added__", nullable = false)
    protected Date addedTime;

    @Column(name = "__order__", nullable = false)
    protected long order;

    //////////////////
    // Deleted Flag //
    @Column(name = "__deleted_flag__", nullable = false)
    protected boolean deleted;

    @Column(name = "__deleted_time__")
    protected Date deletedTime;

    /////////////////
    // Constructor //
    /////////////////

    protected EDevice() {
    }

    protected EDevice(String label, int category, int type,
                   String uid, String address, String info,
                   boolean on, int state) {
        this.label = label;
        this.category = category;
        this.type = type;
        this.uid = uid;
        this.address = address;
        this.info = info;
        this.on = on;
        this.state = state;
        this.addedTime = new Date();
        this.order = 0;
        this.deleted = false;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getAddedTime() {
        return addedTime;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    //////////////
    // Override //
    //////////////

    @Override
    public String toString() {
        return "ESDevice{\"" + id + "\":\"" + label + "\"}";
    }

}
