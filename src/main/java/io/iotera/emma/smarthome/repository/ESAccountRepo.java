package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.account.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ESAccountRepo {

    @Transactional
    public interface ESAccountJRepo extends JpaRepository<ESAccount, Long> {
        ESAccount findByIdAndDeactivateFalse(Long id);

        ESAccount findByUsernameAndDeactivateFalse(String username);

        ESAccount findByEmailAndDeactivateFalse(String email);

        ESAccount findByPhoneNumberAndDeactivateFalse(String phoneNumber);

        ESAccount findByHubTokenAndDeactivateFalse(String token);

        ESAccount findByClientTokenAndDeactivateFalse(String token);

        ESAccount findByGoogleIdAndDeactivateFalse(String googleId);

        ESAccount findByFacebookIdAndDeactivateFalse(String facebookId);
    }

    @Transactional
    public interface ESAccountParuruJRepo extends JpaRepository<ESAccountParuru, Long> {
    }

    @Transactional
    public interface ESAccountClientJRepo extends JpaRepository<ESAccountClient, Long> {
    }

    @Transactional
    public interface ESAccountHubJRepo extends JpaRepository<ESAccountHub, Long> {
    }

    @Transactional
    public interface ESAccountForgotPasswordJRepo extends JpaRepository<ESAccountForgotPassword, Long> {
        ESAccountForgotPassword findByToken(String token);
    }


}
