package com.bijlipay.repository;

import com.bijlipay.model.tickets.IssuesList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IssuesListRepository extends JpaRepository<IssuesList, Long> {
    @Query("select I from IssuesList I WHERE isActive =1")
    List<IssuesList> findAllList();
}