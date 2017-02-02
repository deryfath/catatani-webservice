package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.access.ESAccess;
import io.iotera.emma.smarthome.model.account.ESAccount;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ESAccessRepository {

    @Transactional
    public interface ESAccessJpaRepository extends JpaRepository<ESAccess, String> {
    }

    @PersistenceContext
    EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Object[]> listClientByAccountId(long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("client.*, access.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("client_tbl AS client JOIN ");
        queryBuilder.append("access_tbl AS access ");
        queryBuilder.append("ON client.id = access.client_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.account_id = :account ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("access.__order__ ASC, access.__added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, "accessByAccountId");
        query.setParameter("account", accountId);

        return query.getResultList();
    }

    public List<Object[]> listAccountByClientId(long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("account.*, access.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("account_tbl AS account JOIN ");
        queryBuilder.append("access_tbl AS access ");
        queryBuilder.append("ON account.id = access.account_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client ");
        queryBuilder.append("ORDER BY ");
        queryBuilder.append("access.__order__ ASC, access.__added__ ASC");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, "accessByClientId");
        query.setParameter("client", clientId);

        return query.getResultList();
    }

    public ESAccess findByClientIdAccountId(long clientId, long accountId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("access_tbl AS access ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.account_id = :account");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccess.class);
        query.setParameter("client", clientId);
        query.setParameter("account", accountId);

        return (ESAccess) DataAccessUtils.singleResult(query.getResultList());
    }

    public ESAccount findAccountByAccessToken(String accessToken, long clientId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("account.* ");
        queryBuilder.append("FROM ");
        queryBuilder.append("account_tbl AS account JOIN access_tbl AS access ");
        queryBuilder.append("ON account.id = access.account_id ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("access.__deleted_flag__ = FALSE ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.atoken = :token ");
        queryBuilder.append("AND ");
        queryBuilder.append("access.client_id = :client");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ESAccount.class);
        query.setParameter("client", clientId);
        query.setParameter("token", accessToken);

        return (ESAccount) DataAccessUtils.singleResult(query.getResultList());
    }

}
