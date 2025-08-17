package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.ProposalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProposalDocumentRepository extends JpaRepository<ProposalDocument, Long> {
    
    // Find all documents for a proposal, ordered by upload date
    List<ProposalDocument> findByProposalIdOrderByUploadedAtDesc(Long proposalId);
    
    // Delete all documents for a proposal
    void deleteByProposalId(Long proposalId);
}
