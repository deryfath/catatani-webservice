package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.device.ESDevice;
import io.iotera.emma.smarthome.model.device.ESRoom;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ESRoomRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<ESRoom> listByAccountId(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoom.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :accountId ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoom.class);
        query.setParameter("accountId", ESRoom.parent(accountId));

        return query.getResultList();
    }

    public ESRoom findByRoomId(String roomId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoom.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :accountId");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoom.class);
        query.setParameter("id", roomId);
        query.setParameter("accountId", ESRoom.parent(accountId));

        return (ESRoom) DataAccessUtils.singleResult(query.getResultList());
    }

    public List<ESRoom> findByName(String name, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoom.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("name = :name ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :accountId");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoom.class);
        query.setParameter("name", name);
        query.setParameter("accountId", ESRoom.parent(accountId));

        return query.getResultList();
    }

    public Map<String, String> listRoomName(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("id, name ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoom.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :accountId ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("accountId", ESRoom.parent(accountId));

        Map<String, String> result = new LinkedHashMap<String, String>();
        List<Object[]> qrList = query.getResultList();
        for (Object[] qrObj : qrList) {
            String roomId = (String) qrObj[0];
            String roomName = (String) qrObj[1];
            result.put(roomId, roomName);
        }

        return result;
    }

    @Transactional
    public int deleteChild(Date deletedTime, String roomId, long accountId) {

        int result = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deletedTimeString = sdf.format(deletedTime);

        // Device
        // Build Query
        StringBuilder deviceQueryBuilder = new StringBuilder();
        deviceQueryBuilder.append("UPDATE ");
        deviceQueryBuilder.append(ESDevice.NAME).append(" ");
        deviceQueryBuilder.append("SET ");
        deviceQueryBuilder.append("__deleted_flag__ = TRUE, ");
        deviceQueryBuilder.append("__deleted_time__ = :dtime ");
        deviceQueryBuilder.append("WHERE ");
        deviceQueryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String deviceQueryString = deviceQueryBuilder.toString();
        Query deviceQuery = entityManager.createNativeQuery(deviceQueryString);
        deviceQuery.setParameter("dtime", deletedTimeString);
        deviceQuery.setParameter("parent", ESDevice.parent("%", roomId, accountId));

        result += deviceQuery.executeUpdate();

        return result;
    }

    @Transactional
    public interface ESRoomJRepo extends JpaRepository<ESRoom, String> {
    }

}
