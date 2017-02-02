package io.iotera.emma.model.device;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public class ERoom {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(nullable = false)
    protected String name;

    @Column(nullable = false)
    protected int category;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(length = 1023)
    protected String info;

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

    protected ERoom() {
    }

    protected ERoom(String name, int category, String info) {
        Date now = new Date();

        this.name = name;
        this.category = category;
        this.info = info;
        this.addedTime = now;
        this.order = 0;
        this.deleted = false;

        this.picture = null;
        this.pictureLastUpdated = now;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getPictureLastUpdated() {
        return pictureLastUpdated;
    }

    public void setPictureLastUpdated(Date pictureLastUpdated) {
        this.pictureLastUpdated = pictureLastUpdated;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
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
        return "ESRoom{\"" + id + "\":\"" + name + "\"}";
    }

}
