package io.iotera.emma.smarthome.model.account;

import io.iotera.util.Encrypt;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccount.NAME)
public class ESAccount {

    public static final String NAME = "v2_account_tbl";

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

    ////////////
    // Client //
    @Column(name = "ctoken", nullable = false)
    protected String clientToken;

    /////////
    // Hub //
    @Column(name = "hactive", nullable = false)
    protected boolean hubActive;

    @Column(name = "hactive_time")
    protected Date hubActiveTime;

    @Column(name = "htoken", nullable = false)
    protected String hubToken;

    ///////////
    // Order //
    @Column(name = "__registered__", nullable = false)
    protected Date registeredTime;

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
    protected ESAccountClient accountClient;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountHub accountHub;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountForgotPassword accountForgotPassword;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    protected ESAccountCamera accountCamera;

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

        generateHubToken();
        this.hubActive = false;

        this.registeredTime = now;

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
        this.hubToken = Encrypt.SHA256("Emma-" + (new Date().getTime()));
    }

    public void generateClientToken() {
        this.clientToken = Encrypt.SHA256("Emma-" + (new Date().getTime()));
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

    public boolean isHubActive() {
        return hubActive;
    }

    public void setHubActive(boolean hubActive) {
        this.hubActive = hubActive;
    }

    public Date getHubActiveTime() {
        return hubActiveTime;
    }

    public void setHubActiveDate(Date hubActiveTime) {
        this.hubActiveTime = hubActiveTime;
    }

    public String getHubToken() {
        return hubToken;
    }

    public void setHubToken(String hubToken) {
        this.hubToken = hubToken;
    }

    public Date getRegisteredTime() {
        return registeredTime;
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

    public ESAccountClient getAccountClient() {
        return accountClient;
    }

    public ESAccountHub getAccountHub() {
        return accountHub;
    }

    public ESAccountForgotPassword getAccountForgotPassword() {
        return accountForgotPassword;
    }

    public ESAccountCamera getAccountCamera() {
        return accountCamera;
    }
}
