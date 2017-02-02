package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.client.ESClient;
import io.iotera.emma.smarthome.model.client.ESClientForgotPassword;
import io.iotera.emma.smarthome.model.client.ESClientParuru;
import io.iotera.emma.smarthome.model.client.ESClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ESClientRepository {

    @Transactional
    public interface ESClientJpaRepository extends JpaRepository<ESClient, Long> {
        ESClient findByIdAndDeactivateFalse(long id);

        ESClient findByUsernameAndDeactivateFalse(String username);

        ESClient findByEmailAndDeactivateFalse(String email);

        ESClient findByPhoneNumberAndDeactivateFalse(String phoneNumber);

        ESClient findByFacebookIdAndDeactivateFalse(String googleId);

        ESClient findByClientTokenAndDeactivateFalse(String token);

        ESClient findByGoogleIdAndDeactivateFalse(String googleId);
    }

    @Transactional
    public interface ESClientParuruJpaRepository extends JpaRepository<ESClientParuru, Long> {
    }

    @Transactional
    public interface ESClientProfileJpaRepository extends JpaRepository<ESClientProfile, Long> {
    }

    @Transactional
    public interface ESClientForgotPasswordJpaRepository extends JpaRepository<ESClientForgotPassword, Long> {

        ESClientForgotPassword findByToken(String token);

    }


}
