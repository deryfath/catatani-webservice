package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.application.ESApplicationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class ESApplicationInfoRepository {

    @Transactional
    public interface ESApplicationInfoJpaRepository extends JpaRepository<ESApplicationInfo, Long> {

    }

}
