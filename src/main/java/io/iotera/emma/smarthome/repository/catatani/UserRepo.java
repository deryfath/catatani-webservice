package io.iotera.emma.smarthome.repository.catatani;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.catatani.UserModel;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by nana on 5/19/2017.
 */

@Repository
public class UserRepo {

    @PersistenceContext
    EntityManager entityManager;

    public List<UserModel> findByUsernameAndEmail(String username, String email) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(UserModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("username = :username ");
        queryBuilder.append("AND ");
        queryBuilder.append("email = :email ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, UserModel.class);
        query.setParameter("username", username);
        query.setParameter("email", email);

        return query.getResultList();
    }

    public List<UserModel> findByUsernameAndPassword(String username, String password) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(UserModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("username = :username ");
        queryBuilder.append("AND ");
        queryBuilder.append("password = :password ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, UserModel.class);
        query.setParameter("username", username);
        query.setParameter("password", password);

        return query.getResultList();
    }

    @Transactional
    public int updateUser(String name, String address, String username, String password, String phone, String email, String farmName, int id) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(UserModel.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("name = :name, ");
        queryBuilder.append("address = :address, ");
        queryBuilder.append("farm_name = :farm_name, ");
        queryBuilder.append("email = :email, ");
        queryBuilder.append("username = :username, ");

        if(!password.equalsIgnoreCase("")){
            queryBuilder.append("password = :password, ");
        }

        queryBuilder.append("phone = :phone ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("name", name);
        query.setParameter("address", address);
        query.setParameter("farm_name", farmName);
        query.setParameter("email", email);
        query.setParameter("username", username);

        if(!password.equalsIgnoreCase("")){
            query.setParameter("password", password);
        }
        query.setParameter("phone", phone);
        query.setParameter("id", id);

        return query.executeUpdate();
    }

    @Transactional
    public int updateUserDelivery(String name, String address, String phone, String email, int id) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE ");
        queryBuilder.append(UserModel.NAME).append(" ");
        queryBuilder.append("SET ");
        queryBuilder.append("name = :name, ");
        queryBuilder.append("address = :address, ");
        queryBuilder.append("email = :email, ");
        queryBuilder.append("phone = :phone ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("name", name);
        query.setParameter("address", address);
        query.setParameter("email", email);
        query.setParameter("phone", phone);
        query.setParameter("id", id);

        return query.executeUpdate();
    }

    public UserModel findByUserId(long userId) {

        // Build Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("* ");
        queryBuilder.append("FROM ");
        queryBuilder.append(UserModel.NAME).append(" ");
        queryBuilder.append("WHERE ");
        queryBuilder.append("id = :id ");

        // Execute Query
        String queryString = queryBuilder.toString();
        Query query = entityManager.createNativeQuery(queryString, UserModel.class);
        query.setParameter("id", userId);

        return (UserModel) DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public interface UserJRepo extends JpaRepository<UserModel, String> {
    }
}
