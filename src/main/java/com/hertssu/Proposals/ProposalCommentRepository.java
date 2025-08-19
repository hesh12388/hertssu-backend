package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.ProposalComment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProposalCommentRepository extends JpaRepository<ProposalComment, Long> {
    
    List<ProposalComment> findByProposalIdOrderByCreatedAtAsc(Long proposalId);
    
    void deleteByProposalId(Long proposalId);
}