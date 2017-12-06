package io.iotera.emma.smarthome.repository.catatani;

import io.iotera.emma.smarthome.model.catatani.ComodityModel;
import io.iotera.emma.smarthome.model.catatani.ItemModel;
import io.iotera.emma.smarthome.model.catatani.ItemTypeModel;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by nana on 5/26/2017.
 */
@Repository
public class ComodityRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<ComodityModel> listComodity() {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ComodityModel.NAME).append(" ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ComodityModel.class);


        return query.getResultList();

    }

    public List<ComodityModel> listComodityByCategoryAndUserId(long category, long userId) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ItemModel.NAME).append(" ");
        queryBuilder.append("INNER JOIN ");
        queryBuilder.append(ComodityModel.NAME).append(" ");
        queryBuilder.append("ON comodity_tbl.item_id = item_tbl.id ");
        queryBuilder.append("INNER JOIN ");
        queryBuilder.append(ItemTypeModel.NAME).append(" ");
        queryBuilder.append("ON item_tbl.item_type_id = item_type_tbl.id ");
        queryBuilder.append("WHERE item_type_tbl.id = :category AND comodity_tbl.user_id = :userId ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ComodityModel.class);
        query.setParameter("category", category);
        query.setParameter("userId", userId);

        return query.getResultList();

    }

    @Transactional
    public int removeComodity(long itemId, long userId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM ");
        queryBuilder.append(ComodityModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("item_id = :itemId ");
        queryBuilder.append("AND ");
        queryBuilder.append("user_id = :userId");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("itemId", itemId);
        query.setParameter("userId", userId);

        return query.executeUpdate();
    }

    @Transactional
    public int updateComodity(int stock, int price, String startHarvest, String finishHarvest, String startPlan, String finishPlan, long itemId, long userId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(ComodityModel.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("stock = :stock, ");
        queryBuilder.append("price = :price, ");
        queryBuilder.append("start_harvest = :start_harvest, ");
        queryBuilder.append("finish_harvest = :finish_harvest, ");
        queryBuilder.append("start_plan = :start_plan, ");
        queryBuilder.append("finish_plan = :finish_plan ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("item_id = :item_id AND ");
        queryBuilder.append("user_id = :user_id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("stock", stock);
        query.setParameter("price", price);
        query.setParameter("start_harvest", startHarvest);
        query.setParameter("finish_harvest", finishHarvest);
        query.setParameter("start_plan", startPlan);
        query.setParameter("finish_plan", finishPlan);
        query.setParameter("item_id", itemId);
        query.setParameter("user_id", userId);

        return query.executeUpdate();
    }

    public List<ComodityModel> findByItemIdAndUserId(long itemId, long userId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(ComodityModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("item_id = :itemId ");
        queryBuilder.append("AND ");
        queryBuilder.append("user_id = :userId ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, ComodityModel.class);
        query.setParameter("itemId", itemId);
        query.setParameter("userId", userId);

        return query.getResultList();
    }

    @Transactional
    public interface ComodityJRepo extends JpaRepository<ComodityModel, String> {
    }
}
