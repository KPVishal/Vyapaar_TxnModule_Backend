package com.bijlipay.repository;

import com.bijlipay.model.tickets.Tickets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

public interface TicketsRepository extends JpaRepository<Tickets, Long> {
    Tickets findById(long id);

    @Query("SELECT t FROM Tickets t WHERE t.mid = ?1")
    List<Tickets> getByMid(String mid);

    @Query("SELECT t FROM Tickets t WHERE t.ticketNumber = ?1")
    List<HashMap<String, String>> searchByTicketNumber(String ticketNumber);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and (mid IN ?4 OR vpa IN (?5) )AND (ticketNumber LIKE %?3% or tid LIKE %?3%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPaged(Date fromDate, Date toDate, String keywords, List<String> cMid, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and mid IN ?4 AND (ticketNumber LIKE %?3% or tid LIKE %?3%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedMid(Date fromDate, Date toDate, String keywords, List<String> cMid, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and vpa IN ?4 AND (ticketNumber LIKE %?3% or vpa LIKE %?3%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedVpa(Date fromDate, Date toDate, String keywords, List<String> cMid, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and (mid IN ?3 OR vpa IN ?4)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPaged(Date fromDate, Date toDate, List<String> cMid, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and mid IN ?3")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedMid(Date fromDate, Date toDate, List<String> cMid, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE createdAt >= ?1 and createdAt <= ?2 and vpa IN ?3")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedVpa(Date fromDate, Date toDate, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE (mid IN ?2 OR vpa IN ?3) AND ( ticketNumber LIKE %?1% or tid LIKE %?1%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPaged(String keywords, List<String> cMid, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE mid IN ?2 AND ( ticketNumber LIKE %?1% or tid LIKE %?1%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedMid(String keywords, List<String> cMid, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE vpa IN ?2 AND ( ticketNumber LIKE %?1% or vpa LIKE %?1%)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> searchListOphPagedVpa(String keywords, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE (mid IN ?1 OR vpa IN ?2)")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> findListOphPaged(List<String> cMid, List<String> vpa, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE mid IN ?1")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> findListOphPagedMid(List<String> cMid, Pageable pageable);

    @Query("SELECT t FROM Tickets t WHERE vpa IN ?1")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<HashMap<String, Object>> findListOphPagedVpa(List<String> cMid, Pageable pageable);

    @Query("SELECT t.ticketNumber FROM Tickets t WHERE (status != 'Complete' OR status IS NULL) AND (t.mid IN (?1) OR t.vpa IN (?2)) AND t.createdAt between ?3 AND ?4 ORDER BY t.createdAt DESC")
    List<String> searchTicketNumberByMidVpa(List<String> mId, List<String> vpa, Date lastThreeMonthDate, Date currentDate);

    @Query("SELECT t.ticketNumber FROM Tickets t WHERE (status != 'Complete' OR status IS NULL) AND t.mid IN (?1) AND t.createdAt between ?2 AND ?3 ORDER BY t.createdAt DESC")
    List<String> searchTicketNumberByMid(List<String> mId, Date lastThreeMonthDate, Date currentDate);

    @Query("SELECT t.ticketNumber FROM Tickets t WHERE (status != 'Complete' OR status IS NULL) AND t.vpa IN (?1) AND t.createdAt between ?2 AND ?3 ORDER BY t.createdAt DESC")
    List<String> searchTicketNumberByVpa(List<String> vpa, Date lastThreeMonthDate, Date currentDate);

    @Modifying
    @Transactional
    @Query("UPDATE Tickets SET status = ?2 WHERE ticketNumber = ?1")
    void updateTicketStatus(String ticketNumber, String status);

    @Query("SELECT t FROM Tickets t WHERE ticketNumber = ?1")
    Tickets findByTicketNumber(String ticketNumber);
}
