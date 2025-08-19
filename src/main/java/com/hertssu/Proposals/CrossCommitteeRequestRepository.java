package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.CrossCommitteeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestRepository extends JpaRepository<CrossCommitteeRequest, Long> {
    
    List<CrossCommitteeRequest> findByProposalIdOrderByCreatedAtDesc(Long proposalId);
    
    List<CrossCommitteeRequest> findByTargetCommitteeOrderByCreatedAtDesc(Committee targetCommittee);
    
    List<CrossCommitteeRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    void deleteByProposalId(Long proposalId);
}