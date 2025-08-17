package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.CrossCommitteeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestRepository extends JpaRepository<CrossCommitteeRequest, Long> {
    
    // Find all cross-committee requests for a proposal
    List<CrossCommitteeRequest> findByProposalIdOrderByCreatedAtDesc(Long proposalId);
    
    // Find all cross-committee requests targeting a specific committee
    List<CrossCommitteeRequest> findByTargetCommitteeOrderByCreatedAtDesc(Committee targetCommittee);
    
    // Find cross-committee requests made by a specific user
    List<CrossCommitteeRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    // Delete all cross-committee requests for a proposal
    void deleteByProposalId(Long proposalId);
}