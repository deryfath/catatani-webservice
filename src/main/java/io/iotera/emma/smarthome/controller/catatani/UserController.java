package io.iotera.emma.smarthome.controller.catatani;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.catatani.UserModel;
import io.iotera.emma.smarthome.repository.catatani.UserRepo;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by nana on 5/19/2017.
 */

@RestController
@RequestMapping("/user")
public class UserController extends ESBaseController {

    @Autowired
    UserRepo.UserJRepo userJRepo;

    @Autowired
    UserRepo userRepo;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        // Request Body
        ObjectNode body = payloadObject(entity);
        String name = rget(body, "name");
        String address = rget(body, "address");
        String username = rget(body, "username");
        String password = rget(body, "password");
        String phone = rget(body, "phone");
        String email = rget(body, "email");
        String farmName = rget(body, "farm_name");

        if(!userRepo.findByUsernameAndEmail(username,email).isEmpty()){
            return okJsonFailed(100,"Username and email already register");
        }

        UserModel userModel = new UserModel(name,address,phone,email,username,password,farmName);
        userJRepo.saveAndFlush(userModel);

        response.put("id",userModel.getId());
        response.put("status",200);
        response.put("message","success");

        return okJson(response);

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(HttpEntity<String> entity) {

        ObjectNode response = Json.buildObjectNode();
        // Request Body
        ObjectNode body = payloadObject(entity);
        String username = rget(body, "username");
        String password = rget(body, "password");

        List<UserModel> userModels = userRepo.findByUsernameAndPassword(username,password);

        if(!userModels.isEmpty() && userModels.size() == 1){
            response.put("id",userModels.get(0).getId());
            response.put("status",200);
            response.put("message","Login success");

            return okJson(response);

        }

        return okJsonFailed(101,"username and password doesn't exist");

    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {
        ObjectNode response = Json.buildObjectNode();

        // Request Body
        ObjectNode body = payloadObject(entity);

        String name = rget(body, "name");
        String address = rget(body, "address");
        String username = rget(body, "username");
        String password = rget(body, "password");
        String phone = rget(body, "phone");
        String email = rget(body, "email");
        String farmName = rget(body, "farm_name");
        int id = rget(body, "id",Integer.class);

        UserModel userModel = userRepo.findByUserId(id);
        if(userModel==null){
            return okJsonFailed(404,"user id doesn't exist");
        }

        int updated = userRepo.updateUser(name,address,username,password,phone,email,farmName,id);

        if(updated > 0){
            response.put("id",id);
            response.put("status",200);
            response.put("message","update success");
            return okJson(response);

        }else{
            return okJsonFailed(104,"update data error");
        }

    }

    @RequestMapping(value = "/update/delivery", method = RequestMethod.POST)
    public ResponseEntity updateDeliveryAccount(HttpEntity<String> entity) {
        ObjectNode response = Json.buildObjectNode();

        // Request Body
        ObjectNode body = payloadObject(entity);

        String name = rget(body, "name");
        String address = rget(body, "address");
        String phone = rget(body, "phone");
        String email = rget(body, "email");
        int id = rget(body, "id",Integer.class);

        UserModel userModel = userRepo.findByUserId(id);
        if(userModel==null){
            return okJsonFailed(404,"user id doesn't exist");
        }

        int updated = userRepo.updateUserDelivery(name,address,phone,email,id);

        if(updated > 0){
            response.put("id",id);
            response.put("status",200);
            response.put("message","update success");
            return okJson(response);

        }else{
            return okJsonFailed(104,"update data error");
        }

    }

}
