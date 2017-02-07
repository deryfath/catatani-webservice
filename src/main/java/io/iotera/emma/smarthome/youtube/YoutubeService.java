package io.iotera.emma.smarthome.youtube;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.repository.ESAccountCameraRepository;
import io.iotera.util.Tuple;
import io.iotera.web.spring.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.iotera.util.Json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.simple.JSONObject;
import com.google.api.client.util.DateTime;

@Service
public class YoutubeService extends BaseController {

    private int counter = 1;
    private HttpHeaders headersTransition;

    @Autowired
    ESAccountCameraRepository accountCameraRepository;

    public ResponseEntity getListEvent(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + accessToken);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails&mine=true&maxResults=50";
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }

        ObjectNode responseBody = Json.parseToObjectNode(response.getBody());
        ArrayNode data = Json.buildArrayNode();
        ObjectNode responseBodyJson = Json.buildObjectNode();


        ArrayNode result = (ArrayNode) responseBody.get("items");
        for (int i = 0; i < result.size(); i++) {
//            System.out.println(result.get(i).get("id"));
            ObjectNode dataObject = Json.buildObjectNode();
            dataObject.put("id", result.get(i).get("id").toString().replaceAll("[^\\w\\s\\-_]", ""));
            dataObject.put("title", result.get(i).get("snippet").get("title").toString().replaceAll("[^\\w\\s]", ""));
            dataObject.put("description", result.get(i).get("snippet").get("description").toString().replaceAll("[^\\w\\s]", ""));
            dataObject.put("publishedAt", result.get(i).get("snippet").get("publishedAt").toString().replaceAll("[^\\w\\s\\-/:.]", ""));
            dataObject.put("scheduledStartTime", result.get(i).get("snippet").get("scheduledStartTime").toString().replaceAll("[^\\w\\s\\-/:.]", ""));
            dataObject.put("lifeCycleStatus", result.get(i).get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", ""));
            dataObject.put("privacyStatus", result.get(i).get("status").get("privacyStatus").toString().replaceAll("[^\\w\\s]", ""));
            dataObject.put("recordingStatus", result.get(i).get("status").get("recordingStatus").toString().replaceAll("[^\\w\\s]", ""));
            String linkUrl = result.get(i).get("contentDetails").get("monitorStream").get("embedHtml").toString();
            String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);
//            System.out.println(trimUrl);
            dataObject.put("link", trimUrl);
            String thumbnail = result.get(i).get("snippet").get("thumbnails").get("default").get("url").toString();
            String trimThumbnail = thumbnail.replaceAll("[^\\w\\s\\-/:.?&]", "");
            dataObject.put("thumbnail", trimThumbnail);


            data.add(dataObject);
        }

        responseBodyJson.set("data", data);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);

    }

    public ResponseEntity getEventById(String accessToken, String broadcastId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails&id=" + broadcastId;
        System.out.println(url);
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }

        ObjectNode responseBody = Json.parseToObjectNode(response.getBody());
        ObjectNode responseBodyJson = Json.buildObjectNode();
        ObjectNode dataObject = Json.buildObjectNode();

        ArrayNode result = (ArrayNode) responseBody.get("items");

        dataObject.put("id", result.get(0).get("id").toString().replaceAll("[^\\w\\s\\-_]", ""));
        dataObject.put("title", result.get(0).get("snippet").get("title").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("description", result.get(0).get("snippet").get("description").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("publishedAt", result.get(0).get("snippet").get("publishedAt").toString().replaceAll("[^\\w\\s\\-/:.]", ""));
        dataObject.put("scheduledStartTime", result.get(0).get("snippet").get("scheduledStartTime").toString().replaceAll("[^\\w\\s\\-/:.]", ""));
        dataObject.put("lifeCycleStatus", result.get(0).get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("privacyStatus", result.get(0).get("status").get("privacyStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("recordingStatus", result.get(0).get("status").get("recordingStatus").toString().replaceAll("[^\\w\\s]", ""));
        String linkUrl = result.get(0).get("contentDetails").get("monitorStream").get("embedHtml").toString();
        String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);

        dataObject.put("link", trimUrl);
        String thumbnail = result.get(0).get("snippet").get("thumbnails").get("default").get("url").toString();
        String trimThumbnail = thumbnail.replaceAll("[^\\w\\s\\-/:.?&]", "");
        dataObject.put("thumbnail", trimThumbnail);

        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);

    }

    public ResponseEntity createEvent(String accessToken, String title) {

        System.out.println("MASUK CREATE EVENT");

        //get current date time with Date()
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
        Date date = new Date();

        //BROADCAST
        //construct json broadcast
        JSONObject snippet = new JSONObject();
        HashMap<String, Object> dataSnippet = new HashMap<String, Object>();
        dataSnippet.put("title", title);
        dataSnippet.put("scheduledStartTime", new DateTime(dateFormat.format(date)).toString());

        snippet.putAll(dataSnippet);

        HashMap<String, Object> privacyStatus = new HashMap<String, Object>();
        privacyStatus.put("privacyStatus", "unlisted");

        JSONObject status = new JSONObject();
        status.putAll(privacyStatus);

        HashMap<String, Object> containDetailsData = new HashMap<String, Object>();
        containDetailsData.put("enableEmbed", true);
        containDetailsData.put("enableLowLatency", true);

        JSONObject contentDetails = new JSONObject();
        contentDetails.putAll(containDetailsData);

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("snippet", snippet);
        data.put("status", status);
        data.put("contentDetails", contentDetails);
        JSONObject parent = new JSONObject();

        parent.putAll(data);

//        System.out.println(parent);

        //CREATE BROADCAST
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        String urlBroadcast = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails";

        HttpEntity<String> httpEntityBroadcast = new HttpEntity<String>(parent.toJSONString(), headers);

        ResponseEntity<String> responseBroadcast = null;

        try {
            responseBroadcast = restTemplate.exchange(urlBroadcast, HttpMethod.POST, httpEntityBroadcast, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }


//        System.out.println(responseBroadcast);

        //STREAM
        //Construct json Stream
        JSONObject cdn = new JSONObject();
        HashMap<String, Object> dataCdn = new HashMap<String, Object>();
        dataCdn.put("format", "240p");
        dataCdn.put("ingestionType", "rtmp");
        cdn.putAll(dataCdn);

        JSONObject snippetStream = new JSONObject();
        HashMap<String, Object> dataSnippetStream = new HashMap<String, Object>();
        dataSnippetStream.put("title", "stream " + counter);
        counter++;

        HashMap<String, Object> dataStream = new HashMap<String, Object>();
        dataStream.put("cdn", cdn);
        dataStream.put("snippet", dataSnippetStream);
        JSONObject parentStream = new JSONObject();

        parentStream.putAll(dataStream);

//        System.out.println(parentStream);

        //CREATE STREAM
        String urlStream = "https://www.googleapis.com/youtube/v3/liveStreams?part=snippet,cdn,status";
        HttpEntity<String> httpEntityStream = new HttpEntity<String>(parentStream.toJSONString(), headers);

        ResponseEntity<String> responseStream;

        try {
            responseStream = restTemplate.exchange(urlStream, HttpMethod.POST, httpEntityStream, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }
        //GET BROADCAST ID FOR BIND
        ObjectNode responseBodyBroadcast = Json.parseToObjectNode(responseBroadcast.getBody());
        String broadcastID = responseBodyBroadcast.get("id").toString().replaceAll("[^\\w\\s\\-_]", "");

        //GET STREAM ID FOR BIND
        ObjectNode responseBodyStream = Json.parseToObjectNode(responseStream.getBody());
        String streamID = responseBodyStream.get("id").toString().replaceAll("[^\\w\\s\\-_]", "");

        //BIND STREAM TO BROADCAST
        //Bind broadcast & stream
        String urlBind = "https://www.googleapis.com/youtube/v3/liveBroadcasts/bind?id=" + broadcastID + "&part=id,contentDetails&streamId=" + streamID;

//        System.out.println(urlBind);
        HttpEntity<String> httpEntityBind = new HttpEntity<String>(headers);

        ResponseEntity<String> responseBind;

        try {
            responseBind = restTemplate.exchange(urlBind, HttpMethod.POST, httpEntityBind, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }


        ObjectNode responseBodyJson = Json.buildObjectNode();

        ObjectNode dataObject = Json.buildObjectNode();
        dataObject.put("broadcast_id", broadcastID);
        dataObject.put("stream_id", streamID);
        dataObject.put("title", responseBodyBroadcast.get("snippet").get("title").textValue());
        dataObject.put("thumbnail", responseBodyBroadcast.get("snippet").get("thumbnails").get("default").get("url").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("publishedAt", responseBodyBroadcast.get("snippet").get("publishedAt").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("lifeCycleStatus", responseBodyBroadcast.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("privacyStatus", responseBodyBroadcast.get("status").get("privacyStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("recordingStatus", responseBodyBroadcast.get("status").get("recordingStatus").toString().replaceAll("[^\\w\\s]", ""));
        String linkUrl = responseBodyBroadcast.get("contentDetails").get("monitorStream").get("embedHtml").toString();
        String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);
        dataObject.put("link", trimUrl);
        dataObject.put("stream_key", responseBodyStream.get("cdn").get("ingestionInfo").get("streamName").toString().replaceAll("[^\\w\\s\\-]", ""));
        dataObject.put("ingestion_address", responseBodyStream.get("cdn").get("ingestionInfo").get("ingestionAddress").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("stream_status", responseBodyStream.get("status").get("streamStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("health_stream_status", responseBodyStream.get("status").get("healthStatus").get("status").toString().replaceAll("[^\\w\\s]", ""));

        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);
    }

    public ResponseEntity transitionEvent(String accessToken, String broadcastingID, String streamID, String urlStatus) {

        System.out.println("MASUK TRANSITION EVENT");

        String lifeCycleStatus = "";

        RestTemplate restTemplate = new RestTemplate();
        headersTransition = new HttpHeaders();
        headersTransition.setContentType(MediaType.APPLICATION_JSON);
        headersTransition.set("Authorization", "Bearer " + accessToken);


        if (urlStatus.equalsIgnoreCase("testing")) {

            //CHECK STATUS STREAM
            //GET LIST STREAM BY ID
            String urlStream = "https://www.googleapis.com/youtube/v3/liveStreams?part=status&id=" + streamID;
//            System.out.println(urlStream);
            HttpEntity<String> httpEntityStream = new HttpEntity<String>(headersTransition);

            ResponseEntity<String> responseStream;

            try {
                responseStream = restTemplate.exchange(urlStream, HttpMethod.GET, httpEntityStream, String.class);

            } catch (HttpClientErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            } catch (HttpServerErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            }

            ObjectNode responseBodyStream = Json.parseToObjectNode(responseStream.getBody());
//            System.out.println(responseBodyStream);

            String statusStreaming = responseBodyStream.get("items").get(0).get("status").get("streamStatus").toString().replaceAll("[^\\w\\s]", "");
            String statusHealth = responseBodyStream.get("items").get(0).get("status").get("healthStatus").get("status").toString().replaceAll("[^\\w\\s]", "");
            System.out.println(statusStreaming);
            System.out.println(statusHealth);

            if (!statusHealth.equalsIgnoreCase("noData")) {
                System.out.println("masuk testing");

                //TESTING TRANSITION
//                urlStatus = "testing";
                String urlTransitionTesting = "https://www.googleapis.com/youtube/v3/liveBroadcasts/transition?broadcastStatus=" + urlStatus + "&id=" + broadcastingID + "&part=snippet,contentDetails,status";
//            System.out.println(urlTransitionTesting);
                HttpEntity<String> httpEntityTransitionTesting = new HttpEntity<String>(headersTransition);

                ResponseEntity<String> responseTransistionTesting;

                try {
                    responseTransistionTesting = restTemplate.exchange(urlTransitionTesting, HttpMethod.POST, httpEntityTransitionTesting, String.class);

                } catch (HttpClientErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                } catch (HttpServerErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                }

                ObjectNode responseBodyTransitionTesting = Json.parseToObjectNode(responseTransistionTesting.getBody());

                lifeCycleStatus = responseBodyTransitionTesting.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");
                System.out.println(lifeCycleStatus);

//            new java.util.Timer().schedule(
//                    new java.util.TimerTask() {
//                        @Override
//                        public void run() {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }

                //GET LIFECYCLE STATUS
                String urlGetStatusBroadcast = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=status&id=" + broadcastingID;
                System.out.println(urlGetStatusBroadcast);
                HttpEntity<String> httpEntityGetStatusBroadcast = new HttpEntity<String>(headersTransition);
                RestTemplate restTemplate1 = new RestTemplate();
                ResponseEntity<String> responseGetStatusBroadcast;

                try {
                    responseGetStatusBroadcast = restTemplate1.exchange(urlGetStatusBroadcast, HttpMethod.GET, httpEntityGetStatusBroadcast, String.class);

                } catch (HttpClientErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                } catch (HttpServerErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                }


                ObjectNode responseBodyGetStatusBroadcast = Json.parseToObjectNode(responseGetStatusBroadcast.getBody());

                lifeCycleStatus = responseBodyGetStatusBroadcast.get("items").get(0).get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");

                System.out.println("LIFE CYCLE AFTER DELAY : " + lifeCycleStatus);
                // your code here
                if (lifeCycleStatus.equalsIgnoreCase("testing")) {
                    System.out.println("masuk live");
                    urlStatus = "live";
                    String urlTransitionLive = "https://www.googleapis.com/youtube/v3/liveBroadcasts/transition?broadcastStatus=" + urlStatus + "&id=" + broadcastingID + "&part=snippet,contentDetails,status";
                    System.out.println(urlTransitionLive);
                    HttpEntity<String> httpEntityTransitionLive = new HttpEntity<String>(headersTransition);
                    RestTemplate restTemplateLive = new RestTemplate();

                    ResponseEntity<String> responseTransistionLive = null;

                    try {
                        responseTransistionLive = restTemplateLive.exchange(urlTransitionLive, HttpMethod.POST, httpEntityTransitionLive, String.class);
                        ObjectNode responseBodyTransitionLive = Json.parseToObjectNode(responseTransistionLive.getBody());

                        lifeCycleStatus = responseBodyTransitionLive.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");

                        System.out.println(lifeCycleStatus);

                    } catch (HttpClientErrorException e) {
                        System.out.println(e.getStatusCode());
                        System.out.println(e.getMessage());
                        return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                    } catch (HttpServerErrorException e) {
                        System.out.println(e.getStatusCode());
                        System.out.println(e.getMessage());
                        return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                    }


                } else {
                    lifeCycleStatus = "testStarting live";
                }
//                        }
//                    },
//                    20000
//            );

            } else {
                lifeCycleStatus = statusHealth;
            }

        } else if (urlStatus.equalsIgnoreCase("live")) {
            //CHECK STATUS STREAM
            //GET LIST BROADCAST BY ID
            String urlBroadcast = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=status&id=" + broadcastingID;
//        System.out.println(urlStream);
            HttpEntity<String> httpEntityBroadcast = new HttpEntity<String>(headersTransition);

            ResponseEntity<String> responseBroadcast;

            try {
                responseBroadcast = restTemplate.exchange(urlBroadcast, HttpMethod.GET, httpEntityBroadcast, String.class);

            } catch (HttpClientErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            } catch (HttpServerErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            }

            ObjectNode responseBodyBroadcast = Json.parseToObjectNode(responseBroadcast.getBody());

//        System.out.println(responseBodyStream.get("items").get(0).get("status").get("healthStatus").get("status"));

            lifeCycleStatus = responseBodyBroadcast.get("items").get(0).get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");
            System.out.println(lifeCycleStatus);

            if (lifeCycleStatus.equalsIgnoreCase("testing")) {
                String urlTransitionStart = "https://www.googleapis.com/youtube/v3/liveBroadcasts/transition?broadcastStatus=" + urlStatus + "&id=" + broadcastingID + "&part=snippet,contentDetails,status";
                HttpEntity<String> httpEntityTransitionStart = new HttpEntity<String>(headersTransition);
                ResponseEntity<String> responseTransistionTesting;

                try {
                    responseTransistionTesting = restTemplate.exchange(urlTransitionStart, HttpMethod.POST, httpEntityTransitionStart, String.class);

                } catch (HttpClientErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                } catch (HttpServerErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                }


                ObjectNode responseBodyTransitionTesting = Json.parseToObjectNode(responseTransistionTesting.getBody());

                lifeCycleStatus = responseBodyTransitionTesting.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");
                System.out.println(lifeCycleStatus);

            } else {
                lifeCycleStatus = "invalid transition, stream status still testStarting";
            }
        } else if (urlStatus.equalsIgnoreCase("complete")) {
            //CHECK STATUS STREAM
            //GET LIST BROADCAST BY ID
            String urlBroadcast = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=status&id=" + broadcastingID;
//        System.out.println(urlStream);
            HttpEntity<String> httpEntityBroadcast = new HttpEntity<String>(headersTransition);

            ResponseEntity<String> responseBroadcast;

            try {
                responseBroadcast = restTemplate.exchange(urlBroadcast, HttpMethod.GET, httpEntityBroadcast, String.class);

            } catch (HttpClientErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            } catch (HttpServerErrorException e) {
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
            }


            ObjectNode responseBodyBroadcast = Json.parseToObjectNode(responseBroadcast.getBody());

//        System.out.println(responseBodyStream.get("items").get(0).get("status").get("healthStatus").get("status"));

            lifeCycleStatus = responseBodyBroadcast.get("items").get(0).get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");
            System.out.println(lifeCycleStatus);

            if (lifeCycleStatus.equalsIgnoreCase("live")) {
                String urlTransitionStart = "https://www.googleapis.com/youtube/v3/liveBroadcasts/transition?broadcastStatus=" + urlStatus + "&id=" + broadcastingID + "&part=snippet,contentDetails,status";
                HttpEntity<String> httpEntityTransitionStart = new HttpEntity<String>(headersTransition);

                ResponseEntity<String> responseTransistionTesting;

                try {
                    responseTransistionTesting = restTemplate.exchange(urlTransitionStart, HttpMethod.POST, httpEntityTransitionStart, String.class);

                } catch (HttpClientErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                } catch (HttpServerErrorException e) {
                    System.out.println(e.getStatusCode());
                    System.out.println(e.getMessage());
                    return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
                }


                ObjectNode responseBodyTransitionTesting = Json.parseToObjectNode(responseTransistionTesting.getBody());

                lifeCycleStatus = responseBodyTransitionTesting.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", "");
                System.out.println(lifeCycleStatus);

            } else {
                lifeCycleStatus = "invalid transition, stream status still liveStarting";
            }
        }

        ObjectNode responseBodyJson = Json.buildObjectNode();

        ObjectNode dataObject = Json.buildObjectNode();
        dataObject.put("id", broadcastingID);
        dataObject.put("stream_status", lifeCycleStatus);

        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);

    }


    public String getAccessTokenByRefreshToken(String refreshToken, String clientId, String clientSecret, long accountId) {

        System.out.println("GET REFRESH TOKEN");

        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<String, String>();
        bodyMap.add("refresh_token", refreshToken);
        bodyMap.add("client_id", clientId);
        bodyMap.add("client_secret", clientSecret);
        bodyMap.add("grant_type", "refresh_token");

        System.out.println(bodyMap);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = null;

        try {
            response = restTemplate.postForEntity("https://accounts.google.com/o/oauth2/token", bodyMap, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return e.getMessage();
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return e.getMessage();
        }

        ObjectNode responseBody = Json.parseToObjectNode(response.getBody());

        String accessTokenNew = responseBody.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");

        System.out.println("ACCESS TOKEN NEW : " + accessTokenNew);

        accountCameraRepository.updateAccessTokenByAccountId(accessTokenNew, accountId);

        return accessTokenNew;

    }

    public ResponseEntity getAccessTokenAndRefreshtokenByAuthCode(String authCode, String clientId, String
            clientSecret) {

        int statusCode;

        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<String, String>();
        bodyMap.add("code", authCode);
        bodyMap.add("client_id", clientId);
        bodyMap.add("client_secret", clientSecret);
        bodyMap.add("redirect_uri", "http://localhost:8080/Callback");
        bodyMap.add("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = null;

        System.out.println(bodyMap);
        try {
            response = restTemplate.postForEntity("https://accounts.google.com/o/oauth2/token", bodyMap, String.class, headers);
            statusCode = 200;
        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return response;
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return response;
        }

        ObjectNode responseBodyJson = Json.buildObjectNode();

        ObjectNode responseBody = Json.parseToObjectNode(response.getBody());
        System.out.println(responseBody);
        if (statusCode == 200) {
            String accessToken = responseBody.get("access_token").toString().replaceAll("[^\\w\\s\\-_.]", "");
            String refreshToken = responseBody.get("refresh_token").toString().replaceAll("[^\\w\\s\\-_./]", "");

            System.out.println("ACCESS TOKEN INITIATE : " + accessToken);
            System.out.println("REFRESH TOKEN INITIATE : " + refreshToken);

            responseBodyJson.put("access_token", accessToken);
            responseBodyJson.put("refresh_token", refreshToken);
            responseBodyJson.put("status_code", statusCode);

        } else {
            responseBodyJson.put("status_code", 400);
        }


        return okJson(responseBodyJson);

    }

    public ResponseEntity deleteEventById(String accessToken, String broadcastId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?id=" + broadcastId;
        System.out.println(url);
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }

        ObjectNode responseBodyJson = Json.buildObjectNode();
        ObjectNode dataObject = Json.buildObjectNode();

        dataObject.put("id", broadcastId);
        dataObject.put("message", "deleted");

        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);
    }

    ///////////////////

    public Tuple.T3<Integer, String, String> retrieveAccessTokenAndRefreshToken(String oauthCode, String clientId,
                                                                                String clientSecret) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("code", oauthCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", "http://localhost:8080/Callback");
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> httpEntity = new HttpEntity<Object>(body, headers);
        String url = "https://accounts.google.com/o/oauth2/token";

        RestTemplate restTemplate = new RestTemplate();
        int responseCode;
        String responseBody = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            responseCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpClientErrorException e) {
            responseCode = e.getStatusCode().value();
        } catch (HttpServerErrorException e) {
            responseCode = e.getStatusCode().value();
        }

        if (responseCode == 200) {
            ObjectNode objectBody = Json.parseToObjectNode(responseBody);
            return new Tuple.T3<Integer, String, String>(
                    responseCode,
                    objectBody.get("access_token").textValue(),
                    objectBody.get("refresh_token").textValue()
            );
        }

        return new Tuple.T3<Integer, String, String>(
                responseCode,
                null,
                null
        );
    }

    public Tuple.T2<Integer, String> refreshAccessToken(String refreshToken, String clientId, String clientSecret) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("refresh_token", refreshToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> httpEntity = new HttpEntity<Object>(body, headers);
        String url = "https://accounts.google.com/o/oauth2/token";

        RestTemplate restTemplate = new RestTemplate();
        int responseCode;
        String responseBody = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            responseCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpClientErrorException e) {
            responseCode = e.getStatusCode().value();
        } catch (HttpServerErrorException e) {
            responseCode = e.getStatusCode().value();
        }

        if (responseCode == 200) {
            ObjectNode objectBody = Json.parseToObjectNode(responseBody);
            return new Tuple.T2<Integer, String>(responseCode, objectBody.get("access_token").textValue());
        }

        return new Tuple.T2<Integer, String>(responseCode, null);
    }

    public int deleteEvent(String broadcastId, String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?id=" + broadcastId;

        RestTemplate restTemplate = new RestTemplate();
        int responseCode;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
            responseCode = response.getStatusCode().value();
        } catch (HttpClientErrorException e) {
            responseCode = e.getStatusCode().value();
        } catch (HttpServerErrorException e) {
            responseCode = e.getStatusCode().value();
        }

        return responseCode;
    }

    public Tuple.T2<Integer, ObjectNode> retrieveListEvent(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails&mine=true&maxResults=50";

        RestTemplate restTemplate = new RestTemplate();
        int responseCode;
        String responseBody = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            responseCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpClientErrorException e) {
            responseCode = e.getStatusCode().value();
        } catch (HttpServerErrorException e) {
            responseCode = e.getStatusCode().value();
        }

        if (responseCode == 200) {
            ObjectNode objectBody = Json.parseToObjectNode(responseBody);
            ArrayNode items = (ArrayNode) objectBody.get("items");
            ObjectNode result = Json.buildObjectNode();
            for (int i = 0; i < items.size(); i++) {
                ObjectNode obj = Json.buildObjectNode();
                ObjectNode item = (ObjectNode) items.get(i);

                String id = item.get("id").textValue();
                obj.put("id", id);
                obj.put("title", item.get("snippet").get("title").textValue());
                obj.put("description", item.get("snippet").get("description").textValue());
                obj.put("publishedAt", item.get("snippet").get("publishedAt").textValue());
                obj.put("scheduledStartTime", item.get("snippet").get("scheduledStartTime").textValue());
                obj.put("lifeCycleStatus", item.get("status").get("lifeCycleStatus").textValue());
                obj.put("privacyStatus", item.get("status").get("privacyStatus").textValue());
                obj.put("recordingStatus", item.get("status").get("recordingStatus").textValue());
                String linkUrl = item.get("contentDetails").get("monitorStream").get("embedHtml").textValue();
                String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);
                obj.put("link", trimUrl);
                String thumbnail = item.get("snippet").get("thumbnails").get("default").get("url").textValue();
                obj.put("thumbnail", thumbnail);
                result.set(id, obj);
            }

            return new Tuple.T2<Integer, ObjectNode>(responseCode, result);
        }

        return new Tuple.T2<Integer, ObjectNode>(responseCode, null);
    }

    public Tuple.T2<Integer, ObjectNode> retrieveEvent(String accessToken, String broadcastId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
        String url = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails&id=" + broadcastId;

        RestTemplate restTemplate = new RestTemplate();
        int responseCode;
        String responseBody = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            responseCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (HttpClientErrorException e) {
            responseCode = e.getStatusCode().value();
        } catch (HttpServerErrorException e) {
            responseCode = e.getStatusCode().value();
        }

        if (responseCode == 200) {
            ObjectNode objectBody = Json.parseToObjectNode(responseBody);
            ObjectNode item = (ObjectNode) objectBody.get("items").get(0);

            ObjectNode result = Json.buildObjectNode();
            String id = item.get("id").textValue();
            result.put("id", id);
            result.put("title", item.get("snippet").get("title").textValue());
            result.put("description", item.get("snippet").get("description").textValue());
            result.put("publishedAt", item.get("snippet").get("publishedAt").textValue());
            result.put("scheduledStartTime", item.get("snippet").get("scheduledStartTime").textValue());
            result.put("lifeCycleStatus", item.get("status").get("lifeCycleStatus").textValue());
            result.put("privacyStatus", item.get("status").get("privacyStatus").textValue());
            result.put("recordingStatus", item.get("status").get("recordingStatus").textValue());
            String linkUrl = item.get("contentDetails").get("monitorStream").get("embedHtml").textValue();
            String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);
            result.put("link", trimUrl);
            String thumbnail = item.get("snippet").get("thumbnails").get("default").get("url").textValue();
            result.put("thumbnail", thumbnail);

            return new Tuple.T2<Integer, ObjectNode>(responseCode, result);
        }

        return new Tuple.T2<Integer, ObjectNode>(responseCode, null);
    }

    /*
    public ResponseEntity createEvent(String accessToken, String title) {

        System.out.println("MASUK CREATE EVENT");

        //get current date time with Date()
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
        Date date = new Date();

        //BROADCAST
        //construct json broadcast
        JSONObject snippet = new JSONObject();
        HashMap<String, Object> dataSnippet = new HashMap<String, Object>();
        dataSnippet.put("title", title);
        dataSnippet.put("scheduledStartTime", new DateTime(dateFormat.format(date)).toString());

        snippet.putAll(dataSnippet);

        HashMap<String, Object> privacyStatus = new HashMap<String, Object>();
        privacyStatus.put("privacyStatus", "unlisted");

        JSONObject status = new JSONObject();
        status.putAll(privacyStatus);

        HashMap<String, Object> containDetailsData = new HashMap<String, Object>();
        containDetailsData.put("enableEmbed", true);
        containDetailsData.put("enableLowLatency", true);

        JSONObject contentDetails = new JSONObject();
        contentDetails.putAll(containDetailsData);

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("snippet", snippet);
        data.put("status", status);
        data.put("contentDetails", contentDetails);
        JSONObject parent = new JSONObject();

        parent.putAll(data);

//        System.out.println(parent);

        //CREATE BROADCAST
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        String urlBroadcast = "https://www.googleapis.com/youtube/v3/liveBroadcasts?part=snippet,status,contentDetails";

        HttpEntity<String> httpEntityBroadcast = new HttpEntity<String>(parent.toJSONString(), headers);

        ResponseEntity<String> responseBroadcast = null;

        try {
            responseBroadcast = restTemplate.exchange(urlBroadcast, HttpMethod.POST, httpEntityBroadcast, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }


//        System.out.println(responseBroadcast);

        //STREAM
        //Construct json Stream
        JSONObject cdn = new JSONObject();
        HashMap<String, Object> dataCdn = new HashMap<String, Object>();
        dataCdn.put("format", "240p");
        dataCdn.put("ingestionType", "rtmp");
        cdn.putAll(dataCdn);

        JSONObject snippetStream = new JSONObject();
        HashMap<String, Object> dataSnippetStream = new HashMap<String, Object>();
        dataSnippetStream.put("title", "stream " + counter);
        counter++;

        HashMap<String, Object> dataStream = new HashMap<String, Object>();
        dataStream.put("cdn", cdn);
        dataStream.put("snippet", dataSnippetStream);
        JSONObject parentStream = new JSONObject();

        parentStream.putAll(dataStream);

//        System.out.println(parentStream);

        //CREATE STREAM
        String urlStream = "https://www.googleapis.com/youtube/v3/liveStreams?part=snippet,cdn,status";
        HttpEntity<String> httpEntityStream = new HttpEntity<String>(parentStream.toJSONString(), headers);

        ResponseEntity<String> responseStream;

        try {
            responseStream = restTemplate.exchange(urlStream, HttpMethod.POST, httpEntityStream, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }
        //GET BROADCAST ID FOR BIND
        ObjectNode responseBodyBroadcast = Json.parseToObjectNode(responseBroadcast.getBody());
        String broadcastID = responseBodyBroadcast.get("id").toString().replaceAll("[^\\w\\s\\-_]", "");

        //GET STREAM ID FOR BIND
        ObjectNode responseBodyStream = Json.parseToObjectNode(responseStream.getBody());
        String streamID = responseBodyStream.get("id").toString().replaceAll("[^\\w\\s\\-_]", "");

        //BIND STREAM TO BROADCAST
        //Bind broadcast & stream
        String urlBind = "https://www.googleapis.com/youtube/v3/liveBroadcasts/bind?id=" + broadcastID + "&part=id,contentDetails&streamId=" + streamID;

//        System.out.println(urlBind);
        HttpEntity<String> httpEntityBind = new HttpEntity<String>(headers);

        ResponseEntity<String> responseBind;

        try {
            responseBind = restTemplate.exchange(urlBind, HttpMethod.POST, httpEntityBind, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getMessage());
            return okJsonFailed(Integer.valueOf(e.getStatusCode().toString()), e.getMessage());
        }


        ObjectNode responseBodyJson = Json.buildObjectNode();

        ObjectNode dataObject = Json.buildObjectNode();
        dataObject.put("broadcast_id", broadcastID);
        dataObject.put("stream_id", streamID);
        dataObject.put("title", responseBodyBroadcast.get("snippet").get("title").toString().replaceAll("[^\\w\\s\\:-]", ""));
        dataObject.put("thumbnail", responseBodyBroadcast.get("snippet").get("thumbnails").get("default").get("url").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("publishedAt", responseBodyBroadcast.get("snippet").get("publishedAt").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("lifeCycleStatus", responseBodyBroadcast.get("status").get("lifeCycleStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("privacyStatus", responseBodyBroadcast.get("status").get("privacyStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("recordingStatus", responseBodyBroadcast.get("status").get("recordingStatus").toString().replaceAll("[^\\w\\s]", ""));
        String linkUrl = responseBodyBroadcast.get("contentDetails").get("monitorStream").get("embedHtml").toString();
        String trimUrl = linkUrl.substring(linkUrl.indexOf("src=") + 6, linkUrl.indexOf("frameborder") - 3);
        dataObject.put("link", trimUrl);
        dataObject.put("stream_key", responseBodyStream.get("cdn").get("ingestionInfo").get("streamName").toString().replaceAll("[^\\w\\s\\-]", ""));
        dataObject.put("ingestion_address", responseBodyStream.get("cdn").get("ingestionInfo").get("ingestionAddress").toString().replaceAll("[^\\w\\s\\-/:.?&]", ""));
        dataObject.put("stream_status", responseBodyStream.get("status").get("streamStatus").toString().replaceAll("[^\\w\\s]", ""));
        dataObject.put("health_stream_status", responseBodyStream.get("status").get("healthStatus").get("status").toString().replaceAll("[^\\w\\s]", ""));

        responseBodyJson.set("data", dataObject);
        responseBodyJson.put("status_code", 0);
        responseBodyJson.put("status", "success");

        return okJson(responseBodyJson);
    }
    */

}