package io.iotera.emma.smarthome.repository.catatani;

import io.iotera.emma.smarthome.model.catatani.ItemModel;
import io.iotera.emma.smarthome.model.catatani.ItemTypeModel;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by nana on 5/18/2017.
 */

@Repository
public class ItemRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<ItemModel> listItem() {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ItemModel.NAME).append(" ");
        queryBuilder.append("ORDER BY item_type_id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ItemModel.class);


        return query.getResultList();

    }

    public List<ItemModel> listItemByCategory(long id) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ItemModel.NAME).append(" ");
        queryBuilder.append("WHERE item_type_id = :id ");
        queryBuilder.append("ORDER BY item_type_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ItemModel.class);
        query.setParameter("id", id);


        return query.getResultList();

    }

    public ItemModel findByItemId(long itemId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ItemModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ItemModel.class);
        query.setParameter("id", itemId);

        return (ItemModel) DataAccessUtils.singleResult(query.getResultList());
    }

}
