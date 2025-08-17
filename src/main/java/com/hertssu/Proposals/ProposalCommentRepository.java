package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.ProposalComment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProposalCommentRepository extends JpaRepository<ProposalComment, Long> {
    
    // Find all comments for a proposal, ordered by creation date
    List<ProposalComment> findByProposalIdOrderByCreatedAtAsc(Long proposalId);
    
    // Delete all comments for a proposal
    void deleteByProposalId(Long proposalId);
}