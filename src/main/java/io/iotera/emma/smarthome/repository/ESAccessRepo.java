package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import io.iotera.emma.smarthome.model.account.ESHub;
import io.iotera.util.Tuple;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ESAccessRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<Object[]> listClientByHubId(long hubId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("client.*, access.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccount.NAME).append(" AS client ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESAccess.NAME).append(" AS access ");
        queryBuilder.append("ON client.id = access.client_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("client.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.hub_id = :hub ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("access.__order__ ASC, access.__added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccount.ACCESS_BY_ACCOUNT_NAME);
        query.setParameter("hub", hubId);

        return query.getResultList();
    }

    public List<Object[]> listHubByClientId(long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("hub.*, access.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHub.NAME).append(" AS hub ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESAccess.NAME).append(" AS access ");
        queryBuilder.append("ON hub.id = access.hub_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub.hactive = TRUE ");
        queryBuilder.append("AND ");
        queryBuilder.append("hub.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("access.__order__ ASC, access.__added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHub.ACCESS_BY_HUB_NAME);
        query.setParameter("client", clientId);

        return query.getResultList();
    }

    public ESAccess findAccessByHubAndClient(ESHub hub, long hubId, ESAccount client, long clientId) {

        if (hub.getClient().getId() == clientId) {
            if (hub.isHubActive()) {
                return ESAccess.buildOwnerAccess(hubId, clientId, hub.getHubActiveTime());
            } else {
                return null;
            }
        }

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESAccess.NAME).append(" AS access ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.hub_id = :hub ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccess.class);
        query.setParameter("hub", hubId);
        query.setParameter("client", clientId);

        return (ESAccess) DataAccessUtils.singleResult(query.getResultList());
    }

    public ESHub findHubByAccessTokenAndClientId(String accessToken, long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("hub.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHub.NAME).append(" AS hub ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESAccess.NAME).append(" AS access ");
        queryBuilder.append("ON hub.id = access.hub_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub.hactive = TRUE ");
        queryBuilder.append("AND ");
        queryBuilder.append("hub.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.atoken = :token ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHub.class);
        query.setParameter("token", accessToken);
        query.setParameter("client", clientId);

        return (ESHub) DataAccessUtils.singleResult(query.getResultList());
    }

    public Tuple.T2<ESHub, String> findHubByAccessTokenAdmin(String accessToken, long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("hub.*, access.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESHub.NAME).append(" AS hub ");
        queryBuilder.append("JOIN ");
        queryBuilder.append(ESAccess.NAME).append(" AS access ");
        queryBuilder.append("ON hub.id = access.hub_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("hub.hactive = TRUE ");
        queryBuilder.append("AND ");
        queryBuilder.append("hub.__deactivate_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.atoken = :token ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESHub.ACCESS_BY_HUB_NAME);
        query.setParameter("token", accessToken);
        query.setParameter("client", clientId);

        Object[] result = (Object[]) DataAccessUtils.singleResult(query.getResultList());
        if (result != null) {
            ESHub hub = (ESHub) result[0];
            ESAccess access = (ESAccess) result[1];
            return new Tuple.T2<ESHub, String>(hub, access.permission());
        }
        return new Tuple.T2<ESHub, String>(null, null);

    }

    @Transactional
    public interface ESAccessJRepo extends JpaRepository<ESAccess, String> {
        ESAccess findByAccessTokenAndDeletedFalse(String token);
    }

}
