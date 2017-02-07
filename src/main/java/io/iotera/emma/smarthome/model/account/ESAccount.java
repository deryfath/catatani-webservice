package io.iotera.emma.smarthome.model.account;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.utility.ResourceUtility;
import io.iotera.util.Encrypt;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "account_tbl")
@SqlResultSetMapping(
        name = "accessByClientId",
        entities = {
                @EntityResult(entityClass = ESAccount.class),
                @EntityResult(entityClass = ESAccess.class)
        }
)
public class ESAccount {

    @Id
    @TableGenerator(name = "generate_account_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_account_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String email;

    @Column(name = "phone_number", nullable = false)
    protected String phoneNumber;

    @Column(name = "pass", nullable = false)
    protected String password;

    @Column(name = "htoken", nullable = false)
    protected String hubToken;

    @Column(nullable = false)
    protected String name;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(name = "registered_date", nullable = false)
    protected Date registeredDate;

    /////////////
    // Payment //
    @Column(name = "payment_active", nullable = false)
    protected boolean paymentActive;

    //////////////
    // Verified //
    @Column(nullable = false)
    protected boolean verified;

    /////////////////////
    // Deactivate Flag //
    @Column(name = "__deactivate_flag__", nullable = false)
    protected boolean deactivate;

    @Column(name = "__deactivate_time__")
    protected Date deactivateTime;

    ////////////
    // Column //
    ////////////

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountParuru accountParuru;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountProfile accountProfile;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountLocation accountLocation;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountForgotPassword accountForgotPassword;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountCamera accountCamera;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccount() {
    }

    public ESAccount(String email, String password, String phoneNumber, String name) {
        Date now = new Date();

        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.name = name;

        this.pictureLastUpdated = now;

        this.registeredDate = now;
        generateHubToken();

        // TODO
        // default value are false
        this.paymentActive = true;
        this.verified = true;

        this.deactivate = false;
    }

    ////////////
    // Method //
    ////////////

    public void generateHubToken() {
        this.hubToken = Encrypt.SHA256("Emma" + (new Date().getTime()));
    }

    public String picturePath(String hostPath) {
        if (picture == null || picture.isEmpty()) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHubToken() {
        return hubToken;
    }

    public void setHubToken(String hubToken) {
        this.hubToken = hubToken;
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

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public boolean isPaymentActive() {
        return paymentActive;
    }

    public void setPaymentActive(boolean paymentActive) {
        this.paymentActive = paymentActive;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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

    public ESAccountParuru getAccountParuru() {
        return accountParuru;
    }

    public ESAccountLocation getAccountLocation() {
        return accountLocation;
    }

    public ESAccountProfile getAccountProfile() {
        return accountProfile;
    }

    public ESAccountForgotPassword getAccountForgotPassword() {
        return accountForgotPassword;
    }

    public ESAccountCamera getAccountCamera() {
        return accountCamera;
    }

}
