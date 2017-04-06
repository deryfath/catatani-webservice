package io.iotera.emma.smarthome.model.access;

import io.iotera.emma.smarthome.preference.PermissionPref;
import io.iotera.util.Encrypt;
import io.iotera.util.Random;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccess.NAME)
public class ESAccess {

    public static final String NAME = "v2_access_tbl";

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(name = "hub_id", nullable = false)
    protected long hubId;

    @Column(name = "client_id", nullable = false)
    protected long clientId;

    @Column(columnDefinition = "text")
    protected String permission;

    @Column(name = "atoken", nullable = false)
    protected String accessToken;

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

    protected ESAccess() {
    }

    public ESAccess(long hubId, long clientId, String permission) {
        this.hubId = hubId;
        this.clientId = clientId;
        this.permission = permission;
        generateAccessToken();

        this.addedTime = new Date();
        this.order = 0;
        this.deleted = false;
    }

    public ESAccess(long hubId, long clientId) {
        this(hubId, clientId, "[]");
    }

    protected ESAccess(long hubId, long clientId, String accessToken, String permission, Date addedTime) {
        this.hubId = hubId;
        this.clientId = clientId;
        this.permission = permission;
        this.accessToken = accessToken;

        this.addedTime = addedTime;
        this.order = 0;
        this.deleted = false;
    }

    public static ESAccess buildOwnerAccess(long hubId, long clientId, Date hubActiveTime) {
        ESAccess access = new ESAccess(hubId, clientId, ownerAccessToken(hubId), "[owner]",
                hubActiveTime);
        return access;
    }

    ////////////
    // Method //
    ////////////

    public static String ownerAccessToken(long hubId) {
        String clientIdString = String.valueOf(hubId);
        int prefc = (64 - clientIdString.length()) / 2 - 1;
        int sufc = 64 - prefc - clientIdString.length() - 1;
        return Random.alphaNumeric(prefc) + "/" + hubId + "/" + Random.alphaNumeric(sufc);
    }

    public static boolean isOwnerAccess(String accessToken) {
        return accessToken.contains("/");
    }

    public void generateAccessToken() {
        this.accessToken = Encrypt.SHA256("Emma-" + (new Date().getTime()));
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getId() {
        return id;
    }

    public long getHubId() {
        return hubId;
    }

    public long getClientId() {
        return clientId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getAccessToken() {
        return accessToken;
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

    public String permission() {
        if (permission.contains("owner")) {
            return PermissionPref.OWNER;
        } else if (permission.contains("admin")) {
            return PermissionPref.ADMIN;
        } else {
            return PermissionPref.MEMBER;
        }
    }

}
