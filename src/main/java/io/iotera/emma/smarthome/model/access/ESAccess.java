package io.iotera.emma.smarthome.model.access;

import io.iotera.util.Encrypt;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "access_tbl")
public class ESAccess {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(name = "account_id", nullable = false)
    protected long accountId;

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

    public ESAccess(long accountId, long clientId) {
        this.accountId = accountId;
        this.clientId = clientId;
        accessToken();

        this.addedTime = new Date();
        this.order = 0;
        this.deleted = false;
    }

    ////////////
    // Method //
    ////////////

    public void accessToken() {
        this.accessToken = Encrypt.SHA256("Emma" + (new Date().getTime()));
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
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

}
