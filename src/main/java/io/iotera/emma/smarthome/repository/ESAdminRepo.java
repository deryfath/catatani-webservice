package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.admin.ESAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ESAdminRepo {

    @Transactional
    public interface ESAdminJRepo extends JpaRepository<ESAdmin, Long> {
        ESAdmin findByToken(String token);
    }


}
