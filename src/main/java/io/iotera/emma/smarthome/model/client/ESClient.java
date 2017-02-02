package io.iotera.emma.smarthome.model.client;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.utility.ResourceUtility;
import io.iotera.util.Encrypt;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "client_tbl")
@SqlResultSetMapping(
        name = "accessByAccountId",
        entities = {
                @EntityResult(entityClass = ESClient.class),
                @EntityResult(entityClass = ESAccess.class)
        }
)
public class ESClient {

    @Id
    @TableGenerator(name = "generate_client_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_client_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String username;

    @Column(name = "pass", nullable = false)
    protected String password;

    @Column(name = "ctoken", nullable = false)
    protected String clientToken;

    @Column(nullable = false)
    protected String email;

    @Column(name = "phone_number")
    protected String phoneNumber;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

    @Column(name = "google_id")
    protected String googleId;

    @Column(name = "facebook_id")
    protected String facebookId;

    @Column(name = "registered_date", nullable = false)
    protected Date registeredDate;

    /////////////////////
    // Deactivate Flag //
    @Column(name = "__deactivate_flag__", nullable = false)
    protected boolean deactivate;

    @Column(name = "__deactivate_time__")
    protected Date deactivateTime;

    ////////////
    // Column //
    ////////////

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    protected ESClientParuru clientParuru;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    protected ESClientProfile clientProfile;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    protected ESClientForgotPassword clientForgotPassword;

    /////////////////
    // Constructor //
    /////////////////

    protected ESClient() {
    }

    public ESClient(String username, String password, String email, String phoneNumber,
                    String firstName, String lastName,
                    String googleId, String facebookId) {
        Date now = new Date();

        this.username = username;
        this.password = password;
        this.registeredDate = now;
        generateClientToken();

        this.email = email;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;

        this.pictureLastUpdated = now;

        this.googleId = googleId;
        this.facebookId = facebookId;

        this.deactivate = false;
    }

    ////////////
    // Method //
    ////////////

    public void generateClientToken() {
        this.clientToken = Encrypt.SHA256("Emma" + (new Date().getTime()));
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
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

    public Date getRegisteredDate() {
        return registeredDate;
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

    public ESClientParuru getClientParuru() {
        return clientParuru;
    }

    public ESClientProfile getClientProfile() {
        return clientProfile;
    }

    public ESClientForgotPassword getClientForgotPassword() {
        return clientForgotPassword;
    }

    public void setClientForgotPassword(ESClientForgotPassword clientForgotPassword) {
        this.clientForgotPassword = clientForgotPassword;
    }
}
