package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.device.ESCameraHistory;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.util.Json;
import io.iotera.web.spring.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class ESCameraHistoryRepo extends BaseController {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ESDeviceRepo deviceRepo;


    public List<ESCameraHistory> listHistoryByCameraId(String deviceId, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESCameraHistory.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("history_time DESC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);

        System.out.println("__PARENT__: "+ESCameraHistory.parent(deviceId, "%", hubId));
        query.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));

        return query.getResultList();
    }

    @Transactional
    public ResponseEntity countRowHistoryCamera(String deviceId, long hubId) {

        List<ESCameraHistory> listHistoryId = null;
        ObjectNode deviceObject = Json.buildObjectNode();
        List<ESCameraHistory> listCountRow = null;

        System.out.println("PARENT "+ESCameraHistory.parent(deviceId, "%", hubId));

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESCameraHistory.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY history_time ASC LIMIT 1 ");

        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);
        query.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));
        listHistoryId = query.getResultList();

        if (listHistoryId.size() != 0) {
            deviceObject.put("youtube_id", listHistoryId.get(0).getYoutubeBroadcastId());
        }

        queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESCameraHistory.NAME).append(" ");
        queryBuilder.append("WHERE __parent__ LIKE :parent ");
        queryBuilder.append("AND ");
        queryBuilder.append("__deleted_flag__ = FALSE ");

        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);
        query.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));
        listCountRow = query.getResultList();

        if (listCountRow.size() != 0) {

            deviceObject.put("count", listCountRow.size());
        }

        return okJson(deviceObject);

    }

    @Transactional
    public int deleteFirstRowByDeviceId(String deviceId, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESCameraHistory.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY history_time ASC LIMIT 1 ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));

        return query.executeUpdate();
    }

    @Transactional
    public int updateDeleteStatus(String deviceId, long hubId) {

        int result = 0;
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deletedTimeString = sdf.format(now);

        ESDevice device = deviceRepo.findByDeviceId(deviceId, hubId);

        // Appliance
        // Build Query
        StringBuilder applianceBuilder = new StringBuilder();
        applianceBuilder.append("UPDATE ");
        applianceBuilder.append(ESCameraHistory.NAME).append(" ");
        applianceBuilder.append("SET ");
        applianceBuilder.append("__deleted_flag__ = TRUE, ");
        applianceBuilder.append("__deleted_time__ = :dtime ");
        applianceBuilder.append("WHERE ");
        applianceBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String applianceBuilderString = applianceBuilder.toString();
        Query applianceQuery = entityManager.createNativeQuery(applianceBuilderString);
        applianceQuery.setParameter("dtime", deletedTimeString);
        applianceQuery.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));

        result += applianceQuery.executeUpdate();

        return result;
    }

    @Transactional
    public int updateAndReplaceLabel(String deviceId, long hubId, String newTitle, String oldTitle) {

        int result = 0;

        System.out.println("OLD TITLE : "+oldTitle);
        System.out.println("NEW TITLE : "+newTitle);

//        oldTitle = oldTitle.replaceAll("[^A-Za-z ]+", "");
//        oldTitle = oldTitle.replaceAll("  ", "");
//
//        System.out.println("OLD TITLE REPLACE : "+oldTitle+"|");

        // Appliance
        // Build Query
        StringBuilder applianceBuilder = new StringBuilder();
        applianceBuilder.append("UPDATE ");
        applianceBuilder.append(ESCameraHistory.NAME).append(" ");
        applianceBuilder.append("SET ");
        applianceBuilder.append("youtube_title = REPLACE(youtube_title, :oldtitle, :newtitle) ");
        applianceBuilder.append("WHERE ");
        applianceBuilder.append("__deleted_flag__ = FALSE AND ");
        applianceBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String applianceBuilderString = applianceBuilder.toString();
        Query applianceQuery = entityManager.createNativeQuery(applianceBuilderString);
        applianceQuery.setParameter("newtitle", newTitle);
        applianceQuery.setParameter("parent", ESCameraHistory.parent(deviceId, "%", hubId));
        applianceQuery.setParameter("oldtitle", oldTitle);

        result += applianceQuery.executeUpdate();

        return result;
    }

    @Transactional
    public interface ESCameraHistoryJRepo extends JpaRepository<ESCameraHistory, String> {
    }

}
