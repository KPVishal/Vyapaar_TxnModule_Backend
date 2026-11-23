package com.bijlipay.repository;

import com.bijlipay.model.UserAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserAuthenticationRepo extends JpaRepository<UserAuthentication, Integer> {
    @Query(
            value = "Select * from user_authentication where user_id=?1",
            nativeQuery = true
    )
    UserAuthentication getuser(long l);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE user_authentication SET token = ?1 WHERE id=?2",
            nativeQuery = true
    )
    void update(String jwtToken, Long long1);
}