package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.account.ESHub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ESHubRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<ESHub> listHubByClientId(long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHub.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("client_id = :client ");
        queryBuilder.append("AND ");
        queryBuilder.append("hactive = TRUE ");
        queryBuilder.append("AND ");
        queryBuilder.append("__deactivate_flag__ = FALSE ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("hactive_time ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHub.class);
        query.setParameter("client", clientId);

        return query.getResultList();
    }

    @Transactional
    public interface ESHubJRepo extends JpaRepository<ESHub, String> {

        ESHub findByIdAndDeactivateFalse(Long id);

        ESHub findBySuidAndDeactivateFalse(String suid);

        ESHub findByRegistrationTokenAndHubActiveFalseAndDeactivateFalse(String token);

        ESHub findByHubTokenAndHubActiveTrueAndDeactivateFalse(String token);

    }

}
