package io.iotera.emma.smarthome.model.client;

import javax.persistence.*;

@Entity
@Table(name = "client_forgot_pass_tbl")
public class ESClientForgotPassword {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "forgot_pass_token")
    protected String token;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "client_id")
    protected ESClient client;

    /////////////////
    // Constructor //
    /////////////////

    protected ESClientForgotPassword() {
    }

    public ESClientForgotPassword(ESClient client, long accountId) {
        this.id = accountId;
        this.client = client;
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

    public ESClient getClient() {
        return client;
    }

    public void setClient(ESClient client) {
        this.client = client;
    }
}