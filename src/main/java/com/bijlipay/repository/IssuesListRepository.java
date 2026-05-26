package com.bijlipay.repository;

import com.bijlipay.model.tickets.IssuesList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IssuesListRepository extends JpaRepository<IssuesList, Long> {
    @Query(value = "select I from IssuesList I WHERE isActive =1")
    List<IssuesList> findAllList();
}
