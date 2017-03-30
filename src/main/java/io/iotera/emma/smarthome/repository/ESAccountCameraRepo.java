package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controller.ESBaseController;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountCamera;
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
public class ESAccountCameraRepo extends ESBaseController {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ESAccountCameraJRepo accountCameraJRepo;

    public Tuple.T2<String, String> getAccessTokenAndRefreshToken(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccountCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("account_id = :account_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccountCamera.class);
        query.setParameter("account_id", accountId);

        Object result = DataAccessUtils.singleResult(query.getResultList());
        if (result == null) {
            return null;
        }

        ESAccountCamera resultObjects = (ESAccountCamera) result;

        return new Tuple.T2<String, String>(resultObjects.getAccessToken(), resultObjects.getRefreshToken());
    }

    public boolean isYoutubeIdAvailable(String youtubeId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccountCamera.NAME).append(" as camera ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESAccount.NAME).append(" as account ");
        queryBuilder.append("ON camera.account_id = account.id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("camera.youtube_id = :youtube_id ");
        queryBuilder.append("AND ");
        queryBuilder.append("account.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("account.id != :account_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccountCamera.class);
        query.setParameter("youtube_id", youtubeId);
        query.setParameter("account_id", accountId);

        return query.getResultList().isEmpty();
    }

    public ESAccountCamera findByAccountId(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccountCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("account_id = :account_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccountCamera.class);
        query.setParameter("account_id", accountId);

        return (ESAccountCamera) DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public int updateAccessTokenByAccountId(String accessToken, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(ESAccountCamera.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("access_token = :access_token ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("account_id = :account_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("access_token", accessToken);
        query.setParameter("account_id", accountId);

        return query.executeUpdate();
    }

    @Transactional
    public ResponseEntity YoutubeKey(long accountId) {
        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccountCamera.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("account_id = :account_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccountCamera.class);
        query.setParameter("account_id", accountId);
        List<ESAccountCamera> listAccountCamera = query.getResultList();

        ObjectNode deviceObject = Json.buildObjectNode();
        deviceObject.put("access_token", listAccountCamera.get(0).getAccessToken());
        deviceObject.put("refresh_token", listAccountCamera.get(0).getRefreshToken());
        deviceObject.put("max_history", listAccountCamera.get(0).getMaxHistory());

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
    public interface ESAccountCameraJRepo extends JpaRepository<ESAccountCamera, Long> {

    }

}
