package io.iotera.emma.smarthome.model.client;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "client_profile_tbl")
public class ESClientProfile {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(nullable = false)
    protected int gender;

    @Column(nullable = false)
    protected Date dob;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "client_id")
    protected ESClient client;

    /////////////////
    // Constructor //
    /////////////////

    protected ESClientProfile() {
    }

    public ESClientProfile(int gender, Date dob, ESClient client, long clientId) {
        this.id = clientId;
        this.gender = gender;
        this.dob = dob;
        this.client = client;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public ESClient getClient() {
        return client;
    }

}
