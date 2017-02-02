package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "account_profile_tbl")
public class ESAccountProfile {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column(nullable = false)
    protected int gender;

    @Column(nullable = false)
    protected Date dob;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    private ESAccountProfile() {
    }

    public ESAccountProfile(String firstName, String lastName, int gender, Date dob,
                            ESAccount account, long accountId) {
        this.id = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dob = dob;
        this.account = account;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

}
