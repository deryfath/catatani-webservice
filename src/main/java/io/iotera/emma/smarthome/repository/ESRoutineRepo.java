package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.preference.RoutinePref;
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
public class ESRoutineRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<ESRoutine> listByHubId(long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__order__ ASC, category ASC, routine_trigger ASC, __added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoutine.class);
        query.setParameter("parent", ESRoutine.parent(hubId));

        return query.getResultList();
    }

    public List<ESRoutine> listActiveSchedule() {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("category = :schedule ");
        queryBuilder.append("AND ");
        queryBuilder.append("active = TRUE ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("__added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoutine.class);
        query.setParameter("schedule", RoutinePref.CAT_SCHEDULE);

        return query.getResultList();
    }

    public ESRoutine findByRoutineId(String routineId, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoutine.class);
        query.setParameter("id", routineId);
        query.setParameter("parent", ESRoutine.parent(hubId));

        return (ESRoutine) DataAccessUtils.singleResult(query.getResultList());
    }

    public List<ESRoutine> findByName(String name, long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("name = :name ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESRoutine.class);
        query.setParameter("name", name);
        query.setParameter("parent", ESRoutine.parent(hubId));

        return query.getResultList();
    }

    @Transactional
    public int updateExecuted(String routineId, long hubId) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("last_executed = :now,");
        queryBuilder.append("last_executed_commands = null ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("id = :id ");
        queryBuilder.append("AND ");
        queryBuilder.append("__parent__ LIKE :parent");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("now", sdf.format(new Date()));
        query.setParameter("id", routineId);
        query.setParameter("parent", ESRoutine.parent(hubId));

        return query.executeUpdate();
    }

    @Transactional
    public int updateSuccess(String routineId, boolean success, String commands, long hubId) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(ESRoutine.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("last_executed_commands = :commands");
        if (success) {
            queryBuilder.append(" ,");
            queryBuilder.append("last_succeeded = :now ");
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
        query.setParameter("commands", commands);
        query.setParameter("now", sdf.format(new Date()));
        query.setParameter("id", routineId);
        query.setParameter("parent", ESRoutine.parent(hubId));

        return query.executeUpdate();
    }

    @Transactional
    public interface ESRoutineJRepo extends JpaRepository<ESRoutine, String> {
    }

}
