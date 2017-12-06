package io.iotera.emma.smarthome.repository.catatani;

import io.iotera.emma.smarthome.model.catatani.ItemTypeModel;
import io.iotera.emma.smarthome.model.device.ESDevice;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Created by nana on 5/18/2017.
 */

@Repository
public class ItemTypeRepo {

    @PersistenceContext
    EntityManager entityManager;


    public ItemTypeModel getTypeNameById(long id){

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ItemTypeModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ItemTypeModel.class);
        query.setParameter("id", id);

        return (ItemTypeModel) DataAccessUtils.singleResult(query.getResultList());

    }
}
