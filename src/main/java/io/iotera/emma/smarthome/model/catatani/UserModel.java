package io.iotera.emma.smarthome.model.catatani;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by nana on 5/5/2017.
 */

@Entity
@Table(name = UserModel.NAME)
public class UserModel {

    public static final String NAME = "user_tbl";

    @Id
    @TableGenerator(name = "generate_user_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_user_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "created_time")
    protected Date createdTime;

    @Column(name = "name")
    protected String name;

    @Column(name = "address")
    protected String address;

    @Column(name = "phone")
    protected String phone;

    @Column(name = "email")
    protected String email;

    @Column(name = "username")
    protected String username;

    @Column(name = "password")
    protected String password;

    @Column(name = "farm_name")
    protected String farmName;

    @OneToMany(mappedBy="userModel")
    protected Set<ComodityModel> comodityModels;

    public UserModel() {
    }

    public UserModel(String name, String address, String phone, String email, String username, String password, String farmName) {

        this.createdTime = new Date();
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.password = password;
        this.farmName = farmName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }
}
