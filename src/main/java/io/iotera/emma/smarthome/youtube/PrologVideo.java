package io.iotera.emma.smarthome.youtube;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESDeviceController;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by nana on 12/22/2016.
 */
@Service
public class PrologVideo extends ESDeviceController {

    Process proc;
    String accessToken = "";
    String clientId = "";
    String clientSecret = "";
    String refreshToken = "";

    @Autowired
    ESAccountCameraRepository accountYoutubeCameraRepository;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    Environment env;

    public ResponseEntity runVideoProlog(String title,long accountId) {

        String StreamKey;
        ObjectNode responseBodyJson = Json.buildObjectNode();
        ObjectNode responseBodyTransitionStart = null;

        ResponseEntity responseYoutubeKey = accountYoutubeCameraRepository.YoutubeKey(accountId);
        ObjectNode objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
        accessToken = objectKey.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientId = objectKey.get("client_id").toString().replaceAll("[^\\w\\s\\-_.]", "");
        clientSecret = objectKey.get("client_secret").toString().replaceAll("[^\\w\\s\\-_.]", "");
        refreshToken = objectKey.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");

        System.out.println(accessToken);

        ResponseEntity responseEntityCreate = youtubeService.createEvent(accessToken,title);

        ObjectNode responseBody = Json.parseToObjectNode(responseEntityCreate.getBody().toString());

        int statusCode = Integer.parseInt(responseBody.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
        System.out.println(statusCode);

        if(responseBody.get("status_code") != null && statusCode == 401){
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
            System.out.println("masuk CREATE");
            responseEntityCreate = youtubeService.createEvent(accessToken,title);
            responseBody = Json.parseToObjectNode(responseEntityCreate.getBody().toString());

        }

        StreamKey = responseBody.get("data").get("stream_key").toString().replaceAll("[^\\w\\s\\-_]", "");

        try {
//            proc = Runtime.getRuntime().exec(env.getProperty("ffmpeg.prolog")+" -re -stream_loop -1 -i "+env.getProperty("ffmpeg.prolog.source")+" -tune zerolatency -vcodec libx264 -t 12:00:00 -pix_fmt + -c:v copy -c:a aac -strict experimental -f flv rtmp://a.rtmp.youtube.com/live2/"+StreamKey);
            proc = Runtime.getRuntime().exec(env.getProperty("ffmpeg.prolog")+" -re -stream_loop -1 -i "+env.getProperty("ffmpeg.prolog.source")+" -vcodec libx264 -preset veryfast -maxrate 3000k -bufsize 6000k -pix_fmt yuv420p -g 50 -c:a aac -b:a 160k -ac 2 -ar 44100 -f flv rtmp://a.rtmp.youtube.com/live2/"+StreamKey);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
//            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
//            outputGobbler.start();

            String broadcastID = "", streamID = "";

            try{
                broadcastID = responseBody.get("data").get("broadcast_id").toString().replaceAll("[^\\w\\s\\-_]", "");
                streamID = responseBody.get("data").get("stream_id").toString().replaceAll("[^\\w\\s\\-_]", "");
            }catch (NullPointerException e){
                System.out.println("error : "+e.getMessage());
            }

            //TRANSITION TESTING -> LIVE
            String state = "testing";
            ResponseEntity responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);

            responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());

            statusCode = Integer.parseInt(responseBodyTransitionStart.get("status_code").toString().replaceAll("[^\\w\\s]", ""));
            System.out.println(statusCode);

            if(responseBodyTransitionStart.get("status_code") != null && statusCode == 401){
                System.out.println("UNAUTHORIZED");
                //get access token by Refresh token
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
                responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);
                responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());

            }

            //MQTT MESSAGE IF NO DATA
            String statusNodata = responseBodyTransitionStart.get("data").get("stream_status").toString().replaceAll("[^\\w\\s]", "");

            if(statusNodata.equalsIgnoreCase("noData")){

                while(statusNodata.equalsIgnoreCase("noData")){

                    responseEntityTransitionStart = youtubeService.transitionEvent(accessToken,broadcastID,streamID,state);
                    responseBodyTransitionStart = Json.parseToObjectNode(responseEntityTransitionStart.getBody().toString());

                    statusNodata = responseBodyTransitionStart.get("data").get("stream_status").toString().replaceAll("[^\\w\\s]", "");
                    if(!statusNodata.equalsIgnoreCase("noData")){
                        break;
                    }
                }

            }

            // any error???
//            int exitVal = proc.waitFor();
//            System.out.println("ExitValue: " + exitVal);

        }catch (Throwable e){
            e.printStackTrace();
        }

        responseBodyJson.set("stream_status",responseBodyTransitionStart);
        responseBodyJson.set("stream_data",responseBody);

        return okJson(responseBodyJson);
    }

    public void stopVideoProlog(){

        try{
            proc.destroy();
        }catch (Throwable e){
            System.out.println(e.getMessage());
        }

    }

}

class StreamGobbler extends Thread
{
    InputStream is = null;
    String type = "";
    BufferedReader br = null;
    String line= "";

    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            br = new BufferedReader(new InputStreamReader(is));
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        // make sure our stream is closed and resources will be freed
        try {
            br.close();
        } catch (IOException e) {
        }
    }
}
