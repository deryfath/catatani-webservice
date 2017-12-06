package io.iotera.emma.smarthome.model.catatani;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nana on 5/5/2017.
 */

@Entity
@Table(name = ComodityModel.NAME)
public class ComodityModel{

    public static final String NAME = "comodity_tbl";

    @Id
    @TableGenerator(name = "generate_comodity_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_comodity_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "created_time")
    protected Date createdTime;

    @Column(name = "start_plan")
    protected String startPlan;

    @Column(name = "finish_plan")
    protected String finishPlan;

    @Column(name = "start_harvest")
    protected String startHarvest;

    @Column(name = "finish_harvest")
    protected String finishHarvest;

    @Column(name = "stock")
    protected int stock;

    @Column(name = "price")
    protected int price;

    @ManyToOne
    @JoinColumn(name="item_id")
    protected ItemModel itemModels;

    @ManyToOne
    @JoinColumn(name="user_id")
    protected UserModel userModel;

    public ComodityModel() {
    }

    public ComodityModel(String startPlan, String finishPlan, String startHarvest, String finishHarvest, int stock, int price, ItemModel itemModels, UserModel userModel) {

        this.createdTime = new Date();
        this.startPlan = startPlan;
        this.finishPlan = finishPlan;
        this.startHarvest = startHarvest;
        this.finishHarvest = finishHarvest;
        this.stock = stock;
        this.price = price;
        this.itemModels = itemModels;
        this.userModel = userModel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getStartPlan() {
        return startPlan;
    }

    public void setStartPlan(String startPlan) {
        this.startPlan = startPlan;
    }

    public String getFinishPlan() {
        return finishPlan;
    }

    public void setFinishPlan(String finishPlan) {
        this.finishPlan = finishPlan;
    }

    public String getStartHarvest() {
        return startHarvest;
    }

    public void setStartHarvest(String startHarvest) {
        this.startHarvest = startHarvest;
    }

    public String getFinishHarvest() {
        return finishHarvest;
    }

    public void setFinishHarvest(String finishHarvest) {
        this.finishHarvest = finishHarvest;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public ItemModel getItemModels() {
        return itemModels;
    }

    public void setItemModels(ItemModel itemModels) {
        this.itemModels = itemModels;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
