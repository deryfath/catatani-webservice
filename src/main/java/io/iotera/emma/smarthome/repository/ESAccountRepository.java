package io.iotera.emma.smarthome.repository;

import io.iotera.emma.smarthome.model.account.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ESAccountRepository {

    @Transactional
    public interface ESAccountJpaRepository extends JpaRepository<ESAccount, Long> {
        ESAccount findByIdAndDeactivateFalse(Long id);

        ESAccount findByEmailAndDeactivateFalse(String email);

        ESAccount findByPhoneNumberAndDeactivateFalse(String phoneNumber);

        ESAccount findByHubTokenAndDeactivateFalse(String token);
    }

    @Transactional
    public interface ESAccountParuruJpaRepository extends JpaRepository<ESAccountParuru, Long> {
    }

    @Transactional
    public interface ESAccountProfileJpaRepository extends JpaRepository<ESAccountProfile, Long> {
    }

    @Transactional
    public interface ESAccountLocationJpaRepository extends JpaRepository<ESAccountLocation, Long> {
    }

    @Transactional
    public interface ESAccountForgotPasswordJpaRepository extends JpaRepository<ESAccountForgotPassword, Long> {
        ESAccountForgotPassword findByToken(String token);
    }


}
