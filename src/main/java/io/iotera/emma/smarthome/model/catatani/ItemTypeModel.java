package io.iotera.emma.smarthome.model.catatani;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by nana on 5/18/2017.
 */

@Entity
@Table(name = ItemTypeModel.NAME)
public class ItemTypeModel {

    public static final String NAME = "item_type_tbl";

    @Id
    @TableGenerator(name = "generate_item_type_id", initialValue = 1000000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generate_item_type_id")
    @Column(unique = true, nullable = false)
    protected long id;

    @Column(name = "item_type_name")
    protected String itemTypeName;

    @OneToMany(mappedBy="itemTypeModel")
    protected Set<ItemModel> itemModels;

    public static String getNAME() {
        return NAME;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public Set<ItemModel> getItemModels() {
        return itemModels;
    }

    public void setItemModels(Set<ItemModel> itemModels) {
        this.itemModels = itemModels;
    }
}
