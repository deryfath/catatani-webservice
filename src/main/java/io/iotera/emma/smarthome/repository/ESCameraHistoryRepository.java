package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.camera.ESCameraHistory;
import io.iotera.util.Json;
import io.iotera.web.spring.controller.BaseController;
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
public class ESCameraHistoryRepository extends BaseController {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public List<ESCameraHistory> listCameraHistoryByDeviceId(String deviceId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("camera_history_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("device_id = :device_id ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("history_time DESC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);
        query.setParameter("device_id", deviceId);

        return query.getResultList();
    }

    @Transactional
    public ResponseEntity countRowHistoryCamera(String deviceId) {

        List<ESCameraHistory> listHistoryId = null;
        ObjectNode deviceObject = Json.buildObjectNode();
        List<ESCameraHistory> listCountRow = null;

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("camera_history_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("device_id = :device_id ");
        queryBuilder.append("ORDER BY history_time ASC LIMIT 1 ");

        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);
        query.setParameter("device_id", deviceId);
        listHistoryId = query.getResultList();

        if (listHistoryId.size() != 0) {
            deviceObject.put("youtube_id", listHistoryId.get(0).getYoutubeBroadcastId());
        }

        queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("camera_history_tbl ");
        queryBuilder.append("WHERE device_id = :device_id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__deleted_flag__ = FALSE ");

        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString, ESCameraHistory.class);
        query.setParameter("device_id", deviceId);
        listCountRow = query.getResultList();

        if (listCountRow.size() != 0) {

            deviceObject.put("count", listCountRow.size());
        }

        return okJson(deviceObject);

    }

    @Transactional
    public int deleteFirstRowByDeviceId(String deviceId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE ");
        queryBuilder.append("FROM ");
        queryBuilder.append("camera_history_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("device_id = :device_id ");
        queryBuilder.append("ORDER BY history_time ASC limit 1 ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("device_id", deviceId);

        return query.executeUpdate();
    }

    @Transactional
    public int updateDeleteStatus(String deviceId) {

        int result = 0;
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deletedTimeString = sdf.format(now);

        // Appliance
        // Build Query
        StringBuilder applianceBuilder = new StringBuilder();
        applianceBuilder.append("UPDATE ");
        applianceBuilder.append("camera_history_tbl ");
        applianceBuilder.append("SET ");
        applianceBuilder.append("__deleted_flag__ = TRUE, ");
        applianceBuilder.append("__deleted_time__ = :dtime ");
        applianceBuilder.append("WHERE ");
        applianceBuilder.append("device_id = :device_id");

        // Execute Query
        String applianceBuilderString = applianceBuilder.toString();
        Query applianceQuery = entityManager.createNativeQuery(applianceBuilderString);
        applianceQuery.setParameter("dtime", deletedTimeString);
        applianceQuery.setParameter("device_id", deviceId);

        result += applianceQuery.executeUpdate();

        return result;
    }

    @Transactional
    public interface ESCameraHistoryJpaRepository extends JpaRepository<ESCameraHistory, Long> {
    }

}
