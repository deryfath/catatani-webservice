package io.iotera.emma.smarthome.controller.catatani;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.catatani.ComodityModel;
import io.iotera.emma.smarthome.model.catatani.ItemModel;
import io.iotera.emma.smarthome.model.catatani.UserModel;
import io.iotera.emma.smarthome.repository.catatani.ComodityRepo;
import io.iotera.emma.smarthome.repository.catatani.ItemRepo;
import io.iotera.emma.smarthome.repository.catatani.UserRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by nana on 5/5/2017.
 */

@RestController
@RequestMapping("/comodity")
public class ComodityController extends ESBaseController {

    @Autowired
    ComodityRepo comodityRepo;

    @Autowired
    ComodityRepo.ComodityJRepo comodityJRepo;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    UserRepo userRepo;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity getAllComodityList(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        ArrayNode comodityArray = Json.buildArrayNode();
        List<ComodityModel> comodityModels = comodityRepo.listComodity();

        for (ComodityModel comodityModel : comodityModels){
            ObjectNode comodityObject = Json.buildObjectNode();

            comodityObject.put("item_id", comodityModel.getItemModels().getId());
            comodityObject.put("name", comodityModel.getItemModels().getItemName());
            comodityObject.put("image", comodityModel.getItemModels().getItemImage());
            comodityObject.put("item_type",comodityModel.getItemModels().getItemTypeModel().getItemTypeName());
            comodityObject.put("start_harvest",comodityModel.getStartHarvest().toString());
            comodityObject.put("finish_harvest",comodityModel.getFinishHarvest().toString());
            comodityObject.put("start_plan",comodityModel.getStartPlan().toString());
            comodityObject.put("finish_plan",comodityModel.getFinishPlan().toString());
            comodityObject.put("user_id",comodityModel.getUserModel().getId());

            comodityArray.add(comodityObject);
        }

        response.set("data",comodityArray);
        response.put("status_code",200);
        response.put("message","success");

        return okJson(response);

    }

    @RequestMapping(value = "/list/{category}/{userId}", method = RequestMethod.GET)
    public ResponseEntity getAllComodityListByType(@PathVariable("category") long category, @PathVariable("userId") long userId, HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        ArrayNode comodityArray = Json.buildArrayNode();

        List<ComodityModel> comodityModels = comodityRepo.listComodityByCategoryAndUserId(category,userId);

        for (ComodityModel comodityModel : comodityModels){

            ObjectNode comodityObject = Json.buildObjectNode();

            comodityObject.put("item_id", comodityModel.getItemModels().getId());
            comodityObject.put("name", comodityModel.getItemModels().getItemName());
            comodityObject.put("image", comodityModel.getItemModels().getItemImage());
            comodityObject.put("item_type",comodityModel.getItemModels().getItemTypeModel().getItemTypeName());
            comodityObject.put("start_harvest",comodityModel.getStartHarvest().toString());
            comodityObject.put("finish_harvest",comodityModel.getFinishHarvest().toString());
            comodityObject.put("start_plan",comodityModel.getStartPlan().toString());
            comodityObject.put("finish_plan",comodityModel.getFinishPlan().toString());
            comodityObject.put("user_id",comodityModel.getUserModel().getId());

            comodityArray.add(comodityObject);

        }

        response.set("data",comodityArray);
        response.put("status_code",200);
        response.put("message","success");

        return okJson(response);

    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity insertComodity(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        // Request Body
        ObjectNode body = payloadObject(entity);
        long itemId = rget(body, "item_id",Integer.class);
        int stock = rget(body, "stock",Integer.class);
        int price = rget(body, "price",Integer.class);
        String startHarvest = rget(body, "start_harvest");
        String finishHarvest = rget(body, "finish_harvest");
        String startPlan = rget(body, "start_plan");
        String finishPlan = rget(body, "finish_plan");
        long userId = rget(body, "user_id",Integer.class);

        ItemModel itemModel = itemRepo.findByItemId(itemId);
        UserModel userModel = userRepo.findByUserId(userId);

        if(userModel==null && itemModel==null){
            return okJsonFailed(404,"user id & item id doesn't exist");
        }else if(itemModel==null ){
            return okJsonFailed(404,"item id doesn't exist");
        }else if(userModel==null){
            return okJsonFailed(404,"user id doesn't exist");
        }

        if(!comodityRepo.findByItemIdAndUserId(itemId,userId).isEmpty()){
            return okJsonFailed(100,"comodity already register");
        }

        ComodityModel comodityModel = new ComodityModel(startPlan,finishPlan,startHarvest,finishHarvest,stock,price,itemModel,userModel);
        comodityJRepo.saveAndFlush(comodityModel);

        response.put("id",comodityModel.getId());
        response.put("status",200);
        response.put("message","success");

        return okJson(response);

    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ResponseEntity removeComodity(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();

        // Request Body
        ObjectNode body = payloadObject(entity);

        long itemId = rget(body, "item_id",Integer.class);
        long userId = rget(body, "user_id",Integer.class);

        ItemModel itemModel = itemRepo.findByItemId(itemId);
        UserModel userModel = userRepo.findByUserId(userId);

        if(userModel==null && itemModel==null){
            return okJsonFailed(404,"user id & item id doesn't exist");
        }else if(itemModel==null ){
            return okJsonFailed(404,"item id doesn't exist");
        }else if(userModel==null){
            return okJsonFailed(404,"user id doesn't exist");
        }

        int deleted = comodityRepo.removeComodity(itemId,userId);

        if(deleted > 0){
            response.put("item_id",itemId);
            response.put("user_id",userId);
            response.put("status",200);
            response.put("message","delete success");
            return okJson(response);

        }else{
            return okJsonFailedWithErrorMessage(104,"remove data error","data not found");
        }

    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity updateComodity(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();

        // Request Body
        ObjectNode body = payloadObject(entity);

        int stock = rget(body, "stock",Integer.class);
        int price = rget(body, "price",Integer.class);
        String startHarvest = rget(body, "start_harvest");
        String finishHarvest = rget(body, "finish_harvest");
        String startPlan = rget(body, "start_plan");
        String finishPlan = rget(body, "finish_plan");
        long itemId = rget(body, "item_id",Integer.class);
        long userId = rget(body, "user_id",Integer.class);

        ItemModel itemModel = itemRepo.findByItemId(itemId);
        UserModel userModel = userRepo.findByUserId(userId);

        if(userModel==null && itemModel==null){
            return okJsonFailed(404,"user id & item id doesn't exist");
        }else if(itemModel==null ){
            return okJsonFailed(404,"item id doesn't exist");
        }else if(userModel==null){
            return okJsonFailed(404,"user id doesn't exist");
        }

        int updated = comodityRepo.updateComodity(stock,price,startHarvest,finishHarvest,startPlan,finishPlan,itemId,userId);

        if(updated > 0){
            response.put("item_id",itemId);
            response.put("user_id",userId);
            response.put("status",200);
            response.put("message","update success");
            return okJson(response);

        }else{
            return okJsonFailed(104,"update data error");
        }

    }

}
