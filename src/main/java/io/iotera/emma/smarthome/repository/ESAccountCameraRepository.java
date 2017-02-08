package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESAccountCamera;
import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.util.Json;
import io.iotera.util.Tuple;
import io.iotera.web.spring.controller.BaseController;
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
public class ESAccountCameraRepository extends BaseController {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    ESAccountCameraJpaRepository accountCameraJpaRepository;

    public Tuple.T2<String, String> getAccessTokenAndRefreshToken(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("access_token, refresh_token ");
        queryBuilder.append("FROM ");
        queryBuilder.append("account_camera_tbl ");
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

        Object[] resultObjects = (Object[]) result;
        return new Tuple.T2<String, String>((String) resultObjects[0], (String) resultObjects[1]);
    }

    public boolean isYoutubeIdAvailable(String youtubeId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("account_camera_tbl as camera JOIN ");
        queryBuilder.append("account_tbl AS account ");
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
        queryBuilder.append("account_camera_tbl ");
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
        queryBuilder.append("account_camera_tbl ");
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
        queryBuilder.append("account_camera_tbl ");
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
        queryBuilder.append("application_info_tbl ");

        // Execute Query
        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString, ESApplicationInfo.class);
        List<ESApplicationInfo> listApplicationInfo = query.getResultList();

        deviceObject.put("client_id", listApplicationInfo.get(0).getYoutubeApiClientId());
        deviceObject.put("client_secret", listApplicationInfo.get(0).getYoutubeApiClientSecret());

        return okJson(deviceObject);
    }

    @Transactional
    public String checkAvailabilityGoogleAccount(String youtube_id, String youtube_email, String access_token,
                                                 String refresh_token, ESAccount account) {

        String status = "";

        //check availability account google in account_camera_tbl
        String youtube_idQuote = "\"" + youtube_id + "\"";
        String youtube_emailQuote = "\"" + youtube_email + "\"";

        // Execute Query
        String queryString = "SELECT Count(*) " +
                "FROM account_camera_tbl WHERE youtube_id = " + youtube_idQuote + " AND youtube_email = " + youtube_emailQuote;
        System.out.println(queryString);
        Query query = entityManager.createNativeQuery(queryString);
        System.out.println(query.getSingleResult());
        int countRow = Integer.parseInt(query.getSingleResult().toString());

        //check deactivate flag
        //if true replace first row account youtube camera tbl
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("account_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id ");

        // Execute Query
        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString, ESAccount.class);
        query.setParameter("id", account.getId());
        List<ESAccount> accounts = query.getResultList();

        boolean deactivateFlag = accounts.get(0).isDeactivate();
        System.out.println("FLAG : " + deactivateFlag);
        if (!deactivateFlag && countRow > 0) {
            queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ");
            queryBuilder.append("account_camera_tbl ");
            queryBuilder.append("SET ");
            queryBuilder.append("access_token = :access_token, ");
            queryBuilder.append("refresh_token = :refresh_token, ");
            queryBuilder.append("account_id = :account_id ");
            queryBuilder.append("WHERE ");
            queryBuilder.append("youtube_id = :youtube_id AND ");
            queryBuilder.append("youtube_email = :youtube_email ");

            // Execute Query
            queryString = queryBuilder.toString();

            query = entityManager.createNativeQuery(queryString);
            query.setParameter("access_token", access_token);
            query.setParameter("refresh_token", refresh_token);
            query.setParameter("account_id", account.getId());
            query.setParameter("youtube_id", youtube_id);
            query.setParameter("youtube_email", youtube_email);

            query.executeUpdate();

            status = "success update account";

        } else if (countRow == 0) {
            //save initiate account youtube camera tbl row
            ESAccountCamera accountCamera = new ESAccountCamera(account.getId(), access_token, refresh_token, youtube_id, youtube_email, 24, account);
            accountCameraJpaRepository.saveAndFlush(accountCamera);

            status = "save data succeed";
        } else if (countRow > 0) {
            status = "data already exist";

        } else {
            status = "failed";
        }

        return status;
    }

    /*
    @Transactional
    public ResponseEntity getClientIDAndClientSecret() {
        ObjectNode deviceObject = Json.buildObjectNode();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("application_info_tbl ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESApplicationInfo.class);
        List<ESApplicationInfo> listApplicationInfo = query.getResultList();

        deviceObject.put("client_id", listApplicationInfo.get(0).getYoutubeApiClientId());
        deviceObject.put("client_secret", listApplicationInfo.get(0).getYoutubeApiClientSecret());

        return okJson(deviceObject);
    }
    */


    @Transactional
    public interface ESAccountCameraJpaRepository extends JpaRepository<ESAccountCamera, Long> {

    }

    /*
    @Transactional
    public int updateAccessTokenByAccountId(String accessToken, long accountId) {

        System.out.println("UPDATE ACCESS TOKEN");
        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append("account_camera_tbl ");
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
    */


}
