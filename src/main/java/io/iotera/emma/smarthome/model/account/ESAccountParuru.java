package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;

@Entity
@Table(name = ESAccountParuru.NAME)
public class ESAccountParuru {

    public static final String NAME = "v2_account_paruru_tbl";

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String paruru;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccountParuru() {
    }

    public ESAccountParuru(String paruru, ESAccount account, long accountId) {
        this.id = accountId;
        this.paruru = paruru;
        this.account = account;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getParuru() {
        return paruru;
    }

    public void setParuru(String paruru) {
        this.paruru = paruru;
    }
}
