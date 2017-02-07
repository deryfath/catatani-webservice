package io.iotera.emma.smarthome.controller.hub;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountCamera;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepository;
import io.iotera.emma.smarthome.youtube.YoutubeService;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
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
    ESAccountCameraRepository.ESAccountCameraJpaRepository accountCameraJpaRepository;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    ESApplicationInfoRepository applicationInfoRepository;

    @RequestMapping(value = "/youtube/oauth", method = RequestMethod.POST)
    public ResponseEntity oauth(HttpEntity<String> entity) {

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

        Tuple.T2<String, String> youtubeApis = applicationInfoRepository.getClientIdAndClientSecret();
        String clientId = youtubeApis._1;
        String clientSecret = youtubeApis._2;

        String accessToken = null;
        String refreshToken = null;

        Tuple.T3<Integer, String, String> result =
                youtubeService.retrieveAccessTokenAndRefreshToken(oauthCode, clientId, clientSecret);

        for (int i = 0; i < 5; ++i) {
            if (result._1 == 200) {
                accessToken = result._2;
                refreshToken = result._3;
                break;
            }
        }

        if (accessToken == null || refreshToken == null) {
            return okJsonFailed(-1, "failed_to_generate_youtube_api_token");
        }

        if (!accountCameraRepository.isYoutubeIdAvailable(youtubeId, youtubeEmail)) {
            return okJsonFailed(-2, "youtube_id_not_available");
        }

        int yStatusCode = 403;
        for (int i = 0; i < 5; ++i) {
            Tuple.T2<Integer, ObjectNode> result2 = youtubeService.retrieveListEvent(accessToken);
            if (result2._1 == 200) {
                yStatusCode = 200;
                break;
            }
        }

        if (yStatusCode == 403) {
            return okJsonFailed(-3, "youtube_stream_is_not_activated");
        }

        ESAccountCamera accountCamera = accountCameraRepository.findByAccountId(accountId);
        if (accountCamera == null) {
            accountCamera = new ESAccountCamera(accountId, accessToken, refreshToken, youtubeId,
                    youtubeEmail, 24, account);
        } else {
            accountCamera.setAccessToken(accessToken);
            accountCamera.setRefreshToken(refreshToken);
        }
        accountCameraJpaRepository.saveAndFlush(accountCamera);

        // Response
        ObjectNode response = Json.buildObjectNode();
        response.put("youtube_id", accountCamera.getYoutubeId());
        response.put("youtube_email", accountCamera.getYoutubeEmail());
        response.put("max_history", accountCamera.getMaxHistory());
        response.put("status_desc", "success");
        response.put("status_code", 0);

        return okJson(response);
    }

    /*
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
    */

}
