package com.bijlipay.repository;

import com.bijlipay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE  u.id = ?1")
    Optional<User> findById(long id);

    @Query(value = "SELECT * FROM users WHERE mobile_no = ?1", nativeQuery = true)
    User findBymobileNo(String mobNo);

    @Query("select u from User u where u.id =?1")
    User findByUser(long id);

}
