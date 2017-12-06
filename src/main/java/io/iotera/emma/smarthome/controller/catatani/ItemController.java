package io.iotera.emma.smarthome.controller.catatani;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.catatani.ItemModel;
import io.iotera.emma.smarthome.model.catatani.ItemTypeModel;
import io.iotera.emma.smarthome.repository.catatani.ItemRepo;
import io.iotera.emma.smarthome.repository.catatani.ItemTypeRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by nana on 5/18/2017.
 */

@RestController
@RequestMapping("/item")
public class ItemController extends ESBaseController {

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    ItemTypeRepo itemTypeRepo;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity getAllItemList(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        ArrayNode itemArray = Json.buildArrayNode();
        List<ItemModel> itemModels = itemRepo.listItem();

        for (ItemModel itemModel : itemModels){
            ItemTypeModel itemTypeModel = itemTypeRepo.getTypeNameById(itemModel.getItemTypeModel().getId());
            ObjectNode itemObject = Json.buildObjectNode();

            itemObject.put("item_id", itemModel.getId());
            itemObject.put("item_name", itemModel.getItemName());
            itemObject.put("item_img", itemModel.getItemImage());
            itemObject.put("item_type", itemTypeModel.getItemTypeName());

            itemArray.add(itemObject);
        }

        response.set("data",itemArray);
        response.put("status_code",200);
        response.put("message","success");


        return okJson(response);

    }

    @RequestMapping(value = "/get/{category}", method = RequestMethod.GET)
    public ResponseEntity getItemByCategory(@PathVariable("category") long category, HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        ArrayNode itemArray = Json.buildArrayNode();
        List<ItemModel> itemModels = itemRepo.listItemByCategory(category);

        for (ItemModel itemModel : itemModels){
            ItemTypeModel itemTypeModel = itemTypeRepo.getTypeNameById(itemModel.getItemTypeModel().getId());
            ObjectNode itemObject = Json.buildObjectNode();

            itemObject.put("item_id", itemModel.getId());
            itemObject.put("item_name", itemModel.getItemName());
            itemObject.put("item_img", itemModel.getItemImage());
            itemObject.put("item_type", itemTypeModel.getItemTypeName());

            itemArray.add(itemObject);
        }

        response.set("data",itemArray);
        response.put("status_code",200);
        response.put("message","success");


        return okJson(response);
    }
}
