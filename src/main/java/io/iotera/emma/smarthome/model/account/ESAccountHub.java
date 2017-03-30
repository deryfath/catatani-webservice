package io.iotera.emma.smarthome.model.account;

import io.iotera.emma.smarthome.util.ResourceUtility;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccountHub.NAME)
public class ESAccountHub {

    public static final String NAME = "v2_account_hub_tbl";

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String name;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(length = 511)
    protected String address;

    @Column
    protected String latitude;

    @Column
    protected String longitude;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccountHub() {
    }

    public ESAccountHub(String name, String address, String latitude, String longitude,
                        ESAccount account, long accountId) {
        this.id = accountId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.account = account;
    }

    public static ESAccountHub buildDefault(ESAccount account, long accountId) {
        return new ESAccountHub("My Home", "address", "0.0", "0.0", account, accountId);
    }

    ////////////
    // Method //
    ////////////

    public String picturePath(String hostPath) {
        if (picture == null || picture.isEmpty()) {
            return "";
        }
        return ResourceUtility.resourceImagePath(hostPath, picture);
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
