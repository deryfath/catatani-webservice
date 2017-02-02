package io.iotera.emma.smarthome.model.account;

import javax.persistence.*;

@Entity
@Table(name = "account_location_tbl")
public class ESAccountLocation {

    @Id
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(length = 511)
    protected String address;

    @Column
    protected String latitude;

    @Column
    protected String longitude;

    ////////////
    // Column //
    ////////////

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "account_id")
    protected ESAccount account;

    /////////////////
    // Constructor //
    /////////////////

    protected ESAccountLocation() {
    }

    public ESAccountLocation(String address, String latitude, String longitude, ESAccount account, long accountId) {
        this.id = accountId;
        this.address = (address == null) ? "" : address;
        this.latitude = (latitude == null) ? "0.0" : latitude;
        this.longitude = (longitude == null) ? "0.0" : longitude;
        this.account = account;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
