package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import io.iotera.util.Tuple;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class ESApplicationInfoRepo {

    @PersistenceContext
    EntityManager entityManager;

    public Tuple.T2<String, String> getClientIdAndClientSecret() {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("youtube_api_client_id, youtube_api_client_secret ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ESApplicationInfo.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = 1");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);

        Object result = DataAccessUtils.singleResult(query.getResultList());
        if (result == null) {
            return null;
        }

        Object[] resultObjects = (Object[]) result;

        return new Tuple.T2<String, String>((String) resultObjects[0], (String) resultObjects[1]);
    }

    @Transactional
    public interface ESApplicationInfoJRepo extends JpaRepository<ESApplicationInfo, Long> {

    }

}
