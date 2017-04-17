package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.emma.smarthome.model.account.ESHubCamera;
import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ESHubCameraRepo extends ESBaseController {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ESHubCameraJRepo hubCameraJRepo;

    public Tuple.T2<String, String> getAccessTokenAndRefreshToken(long hubId) {

        System.out.println("HUB ID : " + hubId);
        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHubCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub_id = :hub");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHubCamera.class);
        query.setParameter("hub", hubId);

        Object result = DataAccessUtils.singleResult(query.getResultList());
        if (result == null) {
            return null;
        }

        ESHubCamera resultObjects = (ESHubCamera) result;

        return new Tuple.T2<String, String>(resultObjects.getAccessToken(), resultObjects.getRefreshToken());
    }

    public boolean isYoutubeIdAvailable(String youtubeId, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHubCamera.NAME).append(" as camera ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESHub.NAME).append(" as hub ");
        queryBuilder.append("ON camera.hub_id = hub.id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("camera.youtube_id = :youtube_id ");
        queryBuilder.append("AND ");
        queryBuilder.append("hub.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("hub.id != :hub_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHubCamera.class);
        query.setParameter("youtube_id", youtubeId);
        query.setParameter("hub_id", hubId);

        return query.getResultList().isEmpty();
    }

    public ESHubCamera findByHubId(long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHubCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub_id = :hub_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHubCamera.class);
        query.setParameter("hub_id", hubId);

        return (ESHubCamera) DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public int updateAccessTokenByHubId(String accessToken, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(ESHubCamera.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("access_token = :access_token ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub_id = :hub_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("access_token", accessToken);
        query.setParameter("hub_id", hubId);

        return query.executeUpdate();
    }

    @Transactional
    public ResponseEntity YoutubeKey(long hubId) {
        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHubCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub_id = :hub_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHubCamera.class);
        query.setParameter("hub_id", hubId);
        List<ESHubCamera> listHubCamera = query.getResultList();

        ObjectNode deviceObject = Json.buildObjectNode();
        deviceObject.put("access_token", listHubCamera.get(0).getAccessToken());
        deviceObject.put("refresh_token", listHubCamera.get(0).getRefreshToken());
        deviceObject.put("max_history", listHubCamera.get(0).getMaxHistory());

        queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESApplicationInfo.NAME).append(" ");

        // Execute Query
        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString, ESApplicationInfo.class);
        List<ESApplicationInfo> listApplicationInfo = query.getResultList();

        deviceObject.put("client_id", listApplicationInfo.get(0).getYoutubeApiClientId());
        deviceObject.put("client_secret", listApplicationInfo.get(0).getYoutubeApiClientSecret());

        return okJson(deviceObject);
    }

    @Transactional
    public interface ESHubCameraJRepo extends JpaRepository<ESHubCamera, Long> {

    }


}
