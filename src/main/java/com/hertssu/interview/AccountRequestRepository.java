package com.hertssu.interview;

import com.hertssu.model.AccountRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    
    Optional<AccountRequest> findByInterviewId(UUID interviewId);
    
    List<AccountRequest> findAllByOrderByRequestedAtDesc();
    
    boolean existsByEmail(String email);
    
    boolean existsByInterviewId(UUID interviewId);
}