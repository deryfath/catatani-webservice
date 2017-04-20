package io.iotera.emma.smarthome.model.account;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Encrypt;
import io.iotera.util.Random;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESHub.NAME)
@SqlResultSetMapping(
        name = ESHub.ACCESS_BY_HUB_NAME,
        entities = {
                @EntityResult(entityClass = ESHub.class),
                @EntityResult(entityClass = ESAccess.class)
        }
)
public class ESHub {

    public static final String NAME = "v2_hub_tbl";
    public static final String ACCESS_BY_HUB_NAME = "v2_AccessByHub";

    @Id
    @TableGenerator(name = "generate_hub_id", initialValue = 2000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_hub_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String uid;

    @Column(nullable = false)
    protected String suid;

    @Column(name = "rtoken", nullable = false)
    protected String registrationToken;

    @Column(name = "htoken", nullable = false)
    protected String hubToken;

    /////////
    // Hub //
    @Column(name = "hactive", nullable = false)
    protected boolean hubActive;

    @Column(name = "hactive_time")
    protected Date hubActiveTime;

    //////////
    // Info //
    @Column(nullable = false)
    protected String name;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(length = 511)
    protected String address;

    @Column(nullable = false)
    protected String latitude;

    @Column(nullable = false)
    protected String longitude;

    /////////////
    // Payment //
    @Column(name = "payment_active", nullable = false)
    protected boolean paymentActive;

    /////////////////////
    // Deactivate Flag //
    @Column(name = "__deactivate_flag__", nullable = false)
    protected boolean deactivate;

    @Column(name = "__deactivate_time__")
    protected Date deactivateTime;

    ////////////
    // Column //
    ////////////

    @OneToOne
    @JoinColumn(name = "client_id")
    protected ESAccount client;

    @OneToOne(mappedBy = "hub", fetch = FetchType.LAZY)
    protected ESHubCamera hubCamera;

    /////////////////
    // Constructor //
    /////////////////

    protected ESHub() {
    }

    public ESHub(String uid, String suid) {
        this.uid = uid;
        this.suid = suid;

        generateRegistrationToken();
        generateHubToken();
        this.hubActive = false;

        this.name = getDisplayId();
        this.address = "";
        this.latitude = "0.0";
        this.longitude = "0.0";

        // TODO
        // default value are false
        this.paymentActive = true;

        this.deactivate = false;
    }

    ////////////
    // Method //
    ////////////

    public String getDisplayId() {
        return "emma-hub-" + suid;
    }

    public void generateRegistrationToken() {
        this.registrationToken = Random.alphaNumericLowerCase(128);
    }

    public void generateHubToken() {
        this.hubToken = Encrypt.SHA256("Emma-" + (new Date().getTime()));
    }

    public String picturePath(String hostPath) {
        if (picture == null) {
            return "";
        }
        return ResourceUtility.resourceImagePath(hostPath, picture);
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSuid() {
        return suid;
    }

    public void setSuid(String suid) {
        this.suid = suid;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getHubToken() {
        return hubToken;
    }

    public void setHubToken(String hubToken) {
        this.hubToken = hubToken;
    }

    public boolean isHubActive() {
        return hubActive;
    }

    public void setHubActive(boolean hubActive) {
        this.hubActive = hubActive;
    }

    public Date getHubActiveTime() {
        return hubActiveTime;
    }

    public void setHubActiveTime(Date hubActiveTime) {
        this.hubActiveTime = hubActiveTime;
    }

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

    public boolean isPaymentActive() {
        return paymentActive;
    }

    public void setPaymentActive(boolean paymentActive) {
        this.paymentActive = paymentActive;
    }

    public boolean isDeactivate() {
        return deactivate;
    }

    public void setDeactivate(boolean deactivate) {
        this.deactivate = deactivate;
    }

    public Date getDeactivateTime() {
        return deactivateTime;
    }

    public void setDeactivateTime(Date deactivateTime) {
        this.deactivateTime = deactivateTime;
    }

    public ESAccount getClient() {
        return client;
    }

    public void setClient(ESAccount client) {
        this.client = client;
    }

    public ESHubCamera getHubCamera() {
        return hubCamera;
    }

}
