package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.ProposalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProposalDocumentRepository extends JpaRepository<ProposalDocument, Long> {
    
    List<ProposalDocument> findByProposalIdOrderByUploadedAtDesc(Long proposalId);
    
    void deleteByProposalId(Long proposalId);
}
