package io.iotera.emma.smarthome.model.account;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.util.ResourceUtility;
import io.iotera.util.Encrypt;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccount.NAME)
@SqlResultSetMapping(
        name = ESAccount.ACCESS_BY_ACCOUNT_NAME,
        entities = {
                @EntityResult(entityClass = ESAccount.class),
                @EntityResult(entityClass = ESAccess.class)
        }
)
public class ESAccount {

    public static final String NAME = "v2_account_tbl";
    public static final String ACCESS_BY_ACCOUNT_NAME = "v2_AccessByAccount";

    @Id
    @TableGenerator(name = "generate_account_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_account_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String email;

    @Column(nullable = false)
    protected String username;

    @Column(name = "phone_number", nullable = false)
    protected String phoneNumber;

    @Column(name = "pass", nullable = false)
    protected String password;

    @Column(name = "google_id")
    protected String googleId;

    @Column(name = "facebook_id")
    protected String facebookId;

    @Column(name = "ctoken", nullable = false)
    protected String clientToken;

    //////////
    // Info //
    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(nullable = false)
    protected int gender;

    @Column(nullable = false)
    protected Date dob;

    ///////////
    // Order //
    @Column(name = "__registered__", nullable = false)
    protected Date registeredTime;

    /////////////////////
    // Deactivate Flag //
    @Column(name = "__deactivate_flag__", nullable = false)
    protected boolean deactivate;

    @Column(name = "__deactivate_time__")
    protected Date deactivateTime;

    //////////////
    // Verified //
    @Column(nullable = false)
    protected boolean verified;

    ////////////
    // Column //
    ////////////

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountParuru accountParuru;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountForgotPassword accountForgotPassword;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccount() {
    }

    public ESAccount(String email, String username, String password, String phoneNumber,
                     String googleId, String facebookId) {
        Date now = new Date();

        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.googleId = googleId;
        this.facebookId = facebookId;

        generateClientToken();

        this.firstName = "first";
        this.lastName = "last";
        this.gender = 1;
        this.dob = new Date(0);

        this.registeredTime = now;

        // TODO
        // default value is false
        this.verified = true;

        this.deactivate = false;
    }

    ////////////
    // Method //
    ////////////

    public void generateClientToken() {
        this.clientToken = Encrypt.SHA256("Emma-" + (new Date().getTime()));
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getRegisteredTime() {
        return registeredTime;
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

    public ESAccountForgotPassword getAccountForgotPassword() {
        return accountForgotPassword;
    }

}
