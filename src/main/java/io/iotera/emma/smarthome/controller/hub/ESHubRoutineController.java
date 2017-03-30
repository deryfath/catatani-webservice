package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESRoutineController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub/routine")
public class ESHubRoutineController extends ESRoutineController implements ApplicationEventPublisherAware {

    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @RequestMapping(value = "/listall", method = RequestMethod.GET)
    public ResponseEntity listAll(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Result
        return listAll(accountId);
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public ResponseEntity activate(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);

        return activate(body, accountId);
    }

}
