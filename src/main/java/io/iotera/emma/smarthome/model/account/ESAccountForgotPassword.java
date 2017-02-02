package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;

@Entity
@Table(name = "account_forgot_pass_tbl")
public class ESAccountForgotPassword {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "forgot_pass_token")
    protected String token;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccountForgotPassword() {
    }

    public ESAccountForgotPassword(ESAccount account, long accountId) {
        this.id = accountId;
        this.account = account;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ESAccount getAccount() {
        return account;
    }

    public void setAccount(ESAccount account) {
        this.account = account;
    }
}
