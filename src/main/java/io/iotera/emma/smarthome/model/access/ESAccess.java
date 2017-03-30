package io.iotera.emma.smarthome.model.access;

import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.util.Encrypt;
import io.iotera.util.Random;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccess.NAME)
@SqlResultSetMapping(
        name = "v2_AccessByAccount",
        entities = {
                @EntityResult(entityClass = ESAccount.class),
                @EntityResult(entityClass = ESAccess.class)
        }
)
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

    protected ESAccess(long clientId, String accessToken, String permission, Date addedTime) {
        this.hubId = clientId;
        this.clientId = clientId;
        this.permission = permission;
        this.accessToken = accessToken;

        this.addedTime = addedTime;
        this.order = 0;
        this.deleted = false;
    }

    public static ESAccess buildDefaultAccess(long accountId, Date hubActiveTime) {
        ESAccess access = new ESAccess(accountId, defaultAccessToken(accountId), "[admin]",
                hubActiveTime);
        return access;
    }

    ////////////
    // Method //
    ////////////

    public static String defaultAccessToken(long clientId) {
        String clientIdString = String.valueOf(clientId);
        int prefc = (64 - clientIdString.length()) / 2;
        int sufc = 64 - prefc - clientIdString.length();
        return Random.alphaNumeric(prefc) + clientId + Random.alphaNumeric(sufc);
    }

    public static boolean isDefaultAccess(String accessToken, long clientId) {
        return accessToken.contains(String.valueOf(clientId));
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

    public boolean isAdmin() {
        return (hubId == clientId) || (permission != null && permission.contains("admin"));
    }

}
