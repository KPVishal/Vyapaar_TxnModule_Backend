package com.bijlipay.repository;

import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.TidDto;
import com.bijlipay.model.dto.VpaDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTidVpaDataRepository extends JpaRepository<UserTidVpaData, Long> {
    @Query("select q from UserTidVpaData q where q.vpa =?1")
    UserTidVpaData findByVpa(String payeeVpa);

    @Query("select q from UserTidVpaData q where q.tid =?1")
    UserTidVpaData findByTid(String tid);

    @Query("SELECT new com.bijlipay.model.dto.TidDto(t.tid, t.mid, t.isQR) " +
            "FROM UserTidVpaData t WHERE t.user.id = ?1 AND t.isQR = false")
    List<TidDto> findTidByUserId(long userId);


    @Query("SELECT new com.bijlipay.model.dto.VpaDto(v.vpa, v.isQR) " +
            "FROM UserTidVpaData v WHERE v.user.id = ?1 AND v.isQR = true")
    List<VpaDto> findVpaByUserId(long userId);

    @Query("select q.vpa from UserTidVpaData q where q.user =?1 and q.isQR = true")
    List<String> findByVpaBasedOnUser(User user);

    @Query("select q.tid from UserTidVpaData q where q.user =?1 and q.isQR = ?2")
    List<String> fetchTid(User user, boolean isQR);

    List<UserTidVpaData> findByUser(User sessionUserId);

    List<UserTidVpaData> findByMid(String mid);
    @Query("select q from UserTidVpaData q where q.vpa =?1")
    List<UserTidVpaData> findVpa(String payeeVpa);
    // FIX: JOIN FETCH eagerly loads User in same query - prevents LazyInitializationException
    @Query("SELECT u FROM UserTidVpaData u JOIN FETCH u.user WHERE u.tid = :tid")
    UserTidVpaData findByTidWithUser(@Param("tid") String tid);

    @Query("SELECT u FROM UserTidVpaData u JOIN FETCH u.user WHERE u.vpa = :vpa")
    UserTidVpaData findByVpaWithUser(@Param("vpa") String vpa);
}
