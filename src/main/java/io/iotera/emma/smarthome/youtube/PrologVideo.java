package io.iotera.emma.smarthome.youtube;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESDeviceController;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import io.iotera.web.spring.controller.BaseController;
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
public class PrologVideo extends BaseController {

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
        Tuple.T2<Integer, ObjectNode> responseEntityTransitionStart = null;

        ResponseEntity responseYoutubeKey = accountYoutubeCameraRepository.YoutubeKey(accountId);
        ObjectNode objectKey = Json.parseToObjectNode((responseYoutubeKey.getBody().toString()));
        accessToken = objectKey.get("access_token").textValue();
        clientId = objectKey.get("client_id").textValue();
        clientSecret = objectKey.get("client_secret").textValue();
        refreshToken = objectKey.get("refresh_token").textValue();

        System.out.println(accessToken);

        Tuple.T2<Integer, ObjectNode> responseEntityCreate = youtubeService.createEvent(accessToken,title);

        if(responseEntityCreate._1 == 401){
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
            System.out.println("masuk CREATE");
            responseEntityCreate = youtubeService.createEvent(accessToken,title);

        }else if(responseEntityCreate._1 == 400 || responseEntityCreate._1 == 403){
            return okJsonFailed(responseEntityCreate._1,responseEntityCreate._2.toString());
        }

        System.out.println(responseEntityCreate);
        StreamKey = responseEntityCreate._2.get("data").get("stream_key").textValue();

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
                broadcastID = responseEntityCreate._2.get("data").get("broadcast_id").textValue();
                streamID = responseEntityCreate._2.get("data").get("stream_id").textValue();
            }catch (NullPointerException e){
                System.out.println("error : "+e.getMessage());
            }

            //TRANSITION TESTING -> LIVE
            String state = "testing";
            responseEntityTransitionStart = youtubeService.transitionEventStart(accessToken,broadcastID,streamID,state);

            if(responseEntityTransitionStart._1 == 401){
                System.out.println("UNAUTHORIZED");
                //get access token by Refresh token
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken,clientId,clientSecret,accountId);
                responseEntityTransitionStart = youtubeService.transitionEventStart(accessToken,broadcastID,streamID,state);
            }else if(responseEntityTransitionStart._1 == 400 || responseEntityTransitionStart._1 == 403 || responseEntityTransitionStart._1 == 404){
                return okJsonFailed(responseEntityTransitionStart._1,responseEntityTransitionStart._2.toString());
            }

            //MQTT MESSAGE IF NO DATA
            String statusNodata = responseEntityTransitionStart._2.get("data").get("stream_status").textValue();

            if(statusNodata.equalsIgnoreCase("noData")){

                while(statusNodata.equalsIgnoreCase("noData")){

                    responseEntityTransitionStart = youtubeService.transitionEventStart(accessToken,broadcastID,streamID,state);
                    try{
                        statusNodata = responseEntityTransitionStart._2.get("data").get("stream_status").textValue();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        return okJsonFailed(responseEntityTransitionStart._1,responseEntityTransitionStart._2.toString());
                    }
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

        responseBodyJson.set("stream_status",responseEntityTransitionStart._2);
        responseBodyJson.set("stream_data",responseEntityCreate._2);

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
