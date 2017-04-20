package io.iotera.emma.smarthome.youtube;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESHubCamera;
import io.iotera.emma.smarthome.repository.ESApplicationInfoRepo;
import io.iotera.emma.smarthome.repository.ESHubCameraRepo;
import io.iotera.util.Text;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class PrologVideo {

    @Autowired
    ESHubCameraRepo hubCameraRepo;

    @Autowired
    ESApplicationInfoRepo applicationInfoRepo;

    @Autowired
    YoutubeService youtubeService;

    @Autowired
    Environment env;

    public Tuple.T2<Integer, YoutubeItem> runVideoProlog(String title, long hubId) {

        // Obtain Client Id and Client secret
        Tuple.T2<String, String> youtubeClientApi = applicationInfoRepo.getClientIdAndClientSecret();
        if (youtubeClientApi == null) {
            // Client id or Client secret not available
            return new Tuple.T2<Integer, YoutubeItem>(-1, null);
        }

        String clientId = youtubeClientApi._1;
        String clientSecret = youtubeClientApi._2;

        // Obtain Access token and Refresh token
        ESHubCamera hubCamera = hubCameraRepo.findByHubId(hubId);
        if (hubCamera == null) {
            // Access token or Refresh token not available
            return new Tuple.T2<Integer, YoutubeItem>(-2, null);
        }

        String accessToken = hubCamera.getAccessToken();
        String refreshToken = hubCamera.getRefreshToken();

        System.out.println(accessToken);

        Tuple.T2<Integer, ObjectNode> createResponse = youtubeService.createEvent(accessToken, title);
        int createCode = createResponse._1;
        ObjectNode createBody = createResponse._2;

        if (createCode == 401) {
            System.out.println("UNAUTHORIZED");
            //get access token by Refresh token
            accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);
            System.out.println("masuk CREATE");
            createResponse = youtubeService.createEvent(accessToken, title);

        } else if (createCode != 200) {
            System.out.println("ERROR CREATE EVENT");
            return new Tuple.T2<Integer, YoutubeItem>(createCode, null);
        }

        if (!createBody.has("data")) {
            return new Tuple.T2<Integer, YoutubeItem>(createCode, null);
        }

        String streamKey = createBody.get("data").get("stream_key").asText("");
        streamKey = "rtmp://a.rtmp.youtube.com/live2/" + streamKey;
        String broadcastId = createBody.get("data").get("broadcast_id").asText("");
        String streamId = createBody.get("data").get("stream_id").asText("");
        String youtubeUrl = "https://youtu.be/" + broadcastId;

        if (Text.isEitherEmpty(streamKey, broadcastId, streamId)) {
            // Youtube item not found
            return new Tuple.T2<Integer, YoutubeItem>(-3, null);
        }

        Tuple.T2<Integer, ObjectNode> transitionStartResponse;
        int transitionStartCode;
        ObjectNode transitionStartBody;

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(
                    env.getProperty("ffmpeg.prolog") + " -re -stream_loop -1 -i " +
                            env.getProperty("ffmpeg.prolog.source") + " -vcodec libx264 -preset veryfast -maxrate 3000k -bufsize 6000k -pix_fmt yuv420p -g 50 -c:a aac -b:a 160k -ac 2 -ar 44100 -f flv " + streamKey);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
            errorGobbler.start();

            //TRANSITION TESTING -> LIVE
            String state = "testing";
            transitionStartResponse = youtubeService.transitionEventStart(accessToken, broadcastId, streamId, state);
            transitionStartCode = transitionStartResponse._1;
            transitionStartBody = transitionStartResponse._2;

            if (transitionStartCode == 401) {
                System.out.println("UNAUTHORIZED");

                //get access token by Refresh token
                accessToken = youtubeService.getAccessTokenByRefreshToken(refreshToken, clientId, clientSecret, hubId);

                transitionStartResponse = youtubeService.transitionEventStart(accessToken, broadcastId, streamId, state);
                transitionStartCode = transitionStartResponse._1;
                transitionStartBody = transitionStartResponse._2;

            } else if (transitionStartCode == 400 || transitionStartCode == 403 || transitionStartCode == 404) {
                return new Tuple.T2<Integer, YoutubeItem>(transitionStartCode, null);
            }

            if (!transitionStartBody.has("data")) {
                return new Tuple.T2<Integer, YoutubeItem>(transitionStartCode, null);
            }
            String streamStatus = transitionStartBody.get("data").get("stream_status").asText("");

            while (streamStatus.equalsIgnoreCase("noData")) {
                transitionStartResponse = youtubeService.transitionEventStart(accessToken, broadcastId, streamId, state);
                transitionStartCode = transitionStartResponse._1;
                transitionStartBody = transitionStartResponse._2;
                System.out.println(transitionStartBody);

                try {
                    if (transitionStartBody.get("data") != null) {
                        streamStatus = transitionStartBody.get("data").get("stream_status").textValue();
                        System.out.println(streamStatus);
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    stopVideoProlog(process);
                    return new Tuple.T2<Integer, YoutubeItem>(-13, null);
                }

                if (!streamStatus.equalsIgnoreCase("noData")) {
                    break;
                }
            }

            /*
            if (transitionStartBody.get("data").get("stream_status") != null) {
                System.out.println(transitionStartResponse._2.get("data").get("stream_status"));
            }
            */

            //STOP PROLOG VIDEO
            if (streamStatus.equalsIgnoreCase("live")) {
                stopVideoProlog(process);
            }

        } catch (Throwable e) {
            if (process != null) {
                stopVideoProlog(process);
            }

            e.printStackTrace();
        }

        YoutubeItem youtubeItem = new YoutubeItem(broadcastId, streamId, streamKey, youtubeUrl);

        return new Tuple.T2<Integer, YoutubeItem>(0, youtubeItem);
    }

    public void stopVideoProlog(Process process) {

        if (process != null) {
            process.destroy();
        }
//
//        try {
//        } catch (Throwable e) {
//            System.out.println(e.getMessage());
//        }

    }

}

class StreamGobbler extends Thread {
    InputStream is = null;
    String type = "";
    BufferedReader br = null;
    String line = "";

    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // make sure our stream is closed and resources will be freed
        try {
            br.close();
        } catch (IOException e) {
        }
    }
}