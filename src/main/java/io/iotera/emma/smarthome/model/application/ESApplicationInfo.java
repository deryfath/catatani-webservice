package io.iotera.emma.smarthome.model.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ESApplicationInfo.NAME)
public class ESApplicationInfo {

    public static final String NAME = "application_info_tbl";

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "api_version")
    protected long apiVersion;

    @Column(name = "hub_name")
    protected String hubName;

    @Column(name = "hub_package")
    protected String hubPackage;

    @Column(name = "hub_version")
    protected String hubVersion;

    @Column(name = "hub_database_version")
    protected long hubDatabaseVersion;

    @Column(name = "hub_playstore_url")
    protected String hubPlaystoreUrl;

    @Column(name = "client_name")
    protected String clientName;

    @Column(name = "client_package")
    protected String clientPackage;

    @Column(name = "client_version")
    protected String clientVersion;

    @Column(name = "client_database_version")
    protected long clientDatabaseVersion;

    @Column(name = "client_playstore_url")
    protected String clientPlaystoreUrl;

    @Column(name = "youtube_api_client_id")
    protected String youtubeApiClientId;

    @Column(name = "youtube_api_client_secret")
    protected String youtubeApiClientSecret;

    /////////////////
    // Constructor //
    /////////////////

    protected ESApplicationInfo() {
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(long apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getHubName() {
        return hubName;
    }

    public void setHubName(String hubName) {
        this.hubName = hubName;
    }

    public String getHubPackage() {
        return hubPackage;
    }

    public void setHubPackage(String hubPackage) {
        this.hubPackage = hubPackage;
    }

    public String getHubVersion() {
        return hubVersion;
    }

    public void setHubVersion(String hubVersion) {
        this.hubVersion = hubVersion;
    }

    public long getHubDatabaseVersion() {
        return hubDatabaseVersion;
    }

    public void setHubDatabaseVersion(long hubDatabaseVersion) {
        this.hubDatabaseVersion = hubDatabaseVersion;
    }

    public String getHubPlaystoreUrl() {
        return hubPlaystoreUrl;
    }

    public void setHubPlaystoreUrl(String hubPlaystoreUrl) {
        this.hubPlaystoreUrl = hubPlaystoreUrl;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPackage() {
        return clientPackage;
    }

    public void setClientPackage(String clientPackage) {
        this.clientPackage = clientPackage;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public long getClientDatabaseVersion() {
        return clientDatabaseVersion;
    }

    public void setClientDatabaseVersion(long clientDatabaseVersion) {
        this.clientDatabaseVersion = clientDatabaseVersion;
    }

    public String getClientPlaystoreUrl() {
        return clientPlaystoreUrl;
    }

    public void setClientPlaystoreUrl(String clientPlaystoreUrl) {
        this.clientPlaystoreUrl = clientPlaystoreUrl;
    }

    public String getYoutubeApiClientId() {
        return youtubeApiClientId;
    }

    public void setYoutubeApiClientId(String youtubeApiClientId) {
        this.youtubeApiClientId = youtubeApiClientId;
    }

    public String getYoutubeApiClientSecret() {
        return youtubeApiClientSecret;
    }

    public void setYoutubeApiClientSecret(String youtubeApiClientSecret) {
        this.youtubeApiClientSecret = youtubeApiClientSecret;
    }
}