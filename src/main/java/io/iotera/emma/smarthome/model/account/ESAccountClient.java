package io.iotera.emma.smarthome.model.account;

import io.iotera.emma.smarthome.util.ResourceUtility;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESAccountClient.NAME)
public class ESAccountClient {

    public static final String NAME = "v2_account_client_tbl";

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column
    protected String picture;

    @Column(name = "picture_last_updated")
    protected Date pictureLastUpdated;

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

    protected ESAccountClient() {
    }

    public ESAccountClient(String firstName, String lastName, int gender,
                           Date dob, ESAccount account, long accountId) {
        this.id = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dob = dob;
        this.account = account;
    }

    public static ESAccountClient buildDefault(ESAccount account, long accountId) {
        return new ESAccountClient("first", "last", 1, new Date(0), account, accountId);
    }

    ////////////
    // Method //
    ////////////

    public String picturePath(String hostPath) {
        if (picture == null || picture.isEmpty()) {
            return "";
        }
        return ResourceUtility.resourceImagePath(hostPath, picture);
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getPictureLastUpdated() {
        return pictureLastUpdated;
    }

    public void setPictureLastUpdated(Date pictureLastUpdated) {
        this.pictureLastUpdated = pictureLastUpdated;
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
