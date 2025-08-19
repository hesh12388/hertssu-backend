package com.hertssu.Warnings;

import com.hertssu.model.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    
    // Find all warnings for a specific user (assignee)
    List<Warning> findByAssigneeIdOrderByIssuedDateDesc(Long assigneeId);
}