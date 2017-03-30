package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESAccountController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.repository.ESAccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/hub/account")
public class ESHubAccountController extends ESAccountController implements ApplicationEventPublisherAware {

    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    ESAccountRepo.ESAccountJRepo accountJRepo;
    @Autowired
    ESAccountRepo.ESAccountHubJRepo accountHubJRepo;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @RequestMapping(value = "/get/{attr}", method = RequestMethod.GET)
    public ResponseEntity read(@PathVariable String[] attr, HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        List<String> attrList = Arrays.asList(attr);

        return read(attrList, account);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResponseEntity update(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return update(body, false, account, accountId);
    }

}
