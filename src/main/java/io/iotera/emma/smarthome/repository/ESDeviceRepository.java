package io.iotera.emma.smarthome.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.controlstatus.ACControlStatus;
import io.iotera.emma.smarthome.controlstatus.ControlStatus;
import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.preference.DevicePref;
import io.iotera.util.Json;
import io.iotera.web.spring.controller.BaseController;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class ESDeviceRepository extends BaseController {

    @PersistenceContext
    EntityManager entityManager;

    public List<ESDevice> listByAccountId(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("parent", ESDevice.parent(null, "%", accountId));

        return query.getResultList();
    }

    public List<ESDevice> listByRoomId(String roomId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("parent", ESDevice.parent("%", roomId, accountId));

        return query.getResultList();
    }

    public List<ESDevice> listByCategory(int category, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("category = :category ");
        queryBuilder.append("AND ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("category", category);
        query.setParameter("parent", ESDevice.parent(null, "%", accountId));

        return query.getResultList();
    }

    public ESDevice findByDeviceId(String deviceId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("id", deviceId);
        query.setParameter("parent", ESDevice.parent("%", "%", accountId));

        return (ESDevice) DataAccessUtils.singleResult(query.getResultList());
    }

    public List<ESDevice> findByLabel(String label, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("label = :label ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("label", label);
        query.setParameter("parent", ESDevice.parent("%", "%", accountId));

        return query.getResultList();
    }

    public List<ESDevice> findByUid(String uid, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("uid = :uid ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("uid", uid);
        query.setParameter("parent", ESDevice.parent("%", "%", accountId));

        return query.getResultList();
    }

    public List<ESDevice> findByAddress(String address, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("address = :address ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("address", address);
        query.setParameter("parent", ESDevice.parent("%", "%", accountId));

        return query.getResultList();
    }

    public List<ESDevice> findByType(int type, String remoteId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("type = :type ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("type", type);
        query.setParameter("parent", ESDevice.parent(remoteId, "%", accountId));

        return query.getResultList();
    }

    @Transactional
    public int deleteChild(Date deletedTime, String deviceId, long accountId) {

        int result = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deletedTimeString = sdf.format(deletedTime);

        // Appliance
        // Build Query
        StringBuilder applianceBuilder = new StringBuilder();
        applianceBuilder.append("UPDATE ");
        applianceBuilder.append("device_tbl ");
        applianceBuilder.append("SET ");
        applianceBuilder.append("__deleted_flag__ = TRUE, ");
        applianceBuilder.append("__deleted_time__ = :dtime ");
        applianceBuilder.append("WHERE ");
        applianceBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String applianceBuilderString = applianceBuilder.toString();
        Query applianceQuery = entityManager.createNativeQuery(applianceBuilderString);
        applianceQuery.setParameter("dtime", deletedTimeString);
        applianceQuery.setParameter("parent", ESDevice.parent(deviceId, "%", accountId));

        result += applianceQuery.executeUpdate();

        return result;
    }

    @Transactional
    public int updateAddress(String address, String uid, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("SET ");
        queryBuilder.append("address = :address ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("uid = :uid ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("address", address);
        query.setParameter("uid", uid);
        query.setParameter("parent", ESDevice.parent(null, "%", accountId));

        return query.executeUpdate();
    }

    @Transactional
    public int updateStatus(String deviceId, int category, String control, long accountId) {

        ControlStatus controlStatus;
        if (category == DevicePref.CAT_AC) {
            controlStatus = ACControlStatus.build(control);
        } else {
            controlStatus = ControlStatus.build(control);
        }

        if (!controlStatus.update()) {
            return 0;
        }

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("SET ");
        queryBuilder.append("__status_on__ = :on, ");
        queryBuilder.append("__status_state__ = :state");
        if (controlStatus.updateInfo()) {
            queryBuilder.append(", ");
            queryBuilder.append("info = :info ");
        } else {
            queryBuilder.append(' ');
        }
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("on", controlStatus.isOn());
        query.setParameter("state", controlStatus.getState());
        if (controlStatus.updateInfo()) {
            query.setParameter("info", controlStatus.getInfo());
        }
        query.setParameter("id", deviceId);
        query.setParameter("parent", ESDevice.parent(null, "%", accountId));

        return query.executeUpdate();
    }

    @Transactional
    public int updateStatusInfoDevice(String deviceId, String youtubeId, String ingestionAddress, String streamKey, String streamId, String youtubeUrl, String time) {

        System.out.println("APPEND INFO");
        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");

        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESDevice.class);
        query.setParameter("id", deviceId);
        List<ESDevice> listDevice = query.getResultList();

        String infoTmp = "";

        if (listDevice.size() != 0) {
            infoTmp = listDevice.get(0).getInfo();
        }

        ObjectNode infoObject = Json.parseToObjectNode(infoTmp);
        if (infoObject == null) {
            infoObject = Json.buildObjectNode();
        }

        //get old youtube id
        String oldYotubeId = "";
        if (infoObject.get("ybid") != null) {
            oldYotubeId = infoObject.get("ybid").textValue();
        }

        infoObject.put("yobid", oldYotubeId);
        infoObject.put("ybid", youtubeId);
        infoObject.put("ysid", streamId);
        infoObject.put("ysk", ingestionAddress + "/" + streamKey);
        infoObject.put("yurl", youtubeUrl);
        infoObject.put("tm", time);

        System.out.println("INFO TMP: " + infoObject);

        queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append("device_tbl ");
        queryBuilder.append("SET ");
        queryBuilder.append("info = :info ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");

        // Execute Query
        queryString = queryBuilder.toString();
        query = entityManager.createNativeQuery(queryString);
        query.setParameter("info", infoObject.toString());
        query.setParameter("id", deviceId);

        return query.executeUpdate();
    }

    @Transactional
    public interface ESDeviceJpaRepository extends JpaRepository<ESDevice, String> {
    }

}
