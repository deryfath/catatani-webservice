package io.iotera.emma.smarthome.model.client;

import javax.persistence.*;

@Entity
@Table(name = "client_paruru_tbl")
public class ESClientParuru {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected String paruru;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "client_id")
    protected ESClient client;

    /////////////////
    // Constructor //
    /////////////////

    protected ESClientParuru() {
    }

    public ESClientParuru(String paruru, ESClient client, long clientId) {
        this.id = clientId;
        this.paruru = paruru;
        this.client = client;
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
