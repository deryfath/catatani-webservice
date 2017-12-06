package io.iotera.emma.smarthome.model.catatani;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by nana on 5/5/2017.
 */

@Entity
@Table(name = ItemModel.NAME)
public class ItemModel {

    public static final String NAME = "item_tbl";

    @Id
    @TableGenerator(name = "generate_item_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_item_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "name")
    protected String itemName;

    @Column(name = "image")
    protected String itemImage;

    @ManyToOne
    @JoinColumn(name="item_type_id")
    protected ItemTypeModel itemTypeModel;

    @OneToMany(mappedBy="itemModels")
    protected Set<ComodityModel> comodityModels;

    public ItemModel() {
    }

    public ItemModel(long id, String itemName, String itemImage, ItemTypeModel itemTypeModel) {
        this.id = id;
        this.itemName = itemName;
        this.itemImage = itemImage;
        this.itemTypeModel = itemTypeModel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public ItemTypeModel getItemTypeModel() {
        return itemTypeModel;
    }

    public void setItemTypeModel(ItemTypeModel itemTypeModel) {
        this.itemTypeModel = itemTypeModel;
    }
}
