package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hub/account/camera")
public class ESHubAccountCameraController extends ESBaseController {

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESAccountCameraRepository accountYoutubeCameraRepository;

    @Autowired
    ESApplicationInfoRepository.ESApplicationInfoJpaRepository applicationInfoJpaRepository;

    @RequestMapping(value = "/youtube/oauth", method = RequestMethod.POST)
    public ResponseEntity listAll(HttpEntity<String> entity) {

        // Request Header
        //authenticateToken(entity);
        String hubToken = hubToken(entity);

        // Account
        ESAccount account = accountHub(hubToken);
        long accountId = account.getId();

        // Request Body
        ObjectNode body = payloadObject(entity);
        String oauthCode = rget(body, "esoauth");
        String youtubeId = rget(body, "esyid");
        String youtubeEmail = rget(body, "esyemail");

        ObjectNode response = Json.buildObjectNode();

        ResponseEntity<String> applicationInfo = accountYoutubeCameraRepository.getClientIDAndClientSecret();
        ObjectNode responseBody = Json.parseToObjectNode(applicationInfo.getBody());
        String clientSecret = responseBody.get("client_secret").toString().replaceAll("[^\\w\\s\\-_.]", "");
        String clientId = responseBody.get("client_id").toString().replaceAll("[^\\w\\s\\-_.]", "");

        //get access token and refresh token initiate
        ResponseEntity<String> responseAccountYoutube =  youtubeService.getAccessTokenAndRefreshtokenByAuthCode(oauthCode,clientId,clientSecret);
        System.out.println(responseAccountYoutube);

        try {
            ObjectNode responseYoutubeObject = Json.parseToObjectNode(responseAccountYoutube.getBody());
            int statusCode = Integer.parseInt(responseYoutubeObject.get("status_code").toString());
            System.out.println("status code : "+statusCode);
            if(statusCode == 200){
                String accessToken = responseYoutubeObject.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
                String refreshToken = responseYoutubeObject.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");

                //check table account youtube camera
                String status = accountYoutubeCameraRepository.checkAvailabilityGoogleAccount(youtubeId,youtubeEmail,accessToken,refreshToken,account);

                response.put("status",200);
                response.put("message",status);

            }else{
                response.put("status",400);
                response.put("message","bad request");

            }
        }catch (NullPointerException e){
            response.put("status",400);
            response.put("message","bad request");
        }



        // Response


        return okJson(response);
    }

}
