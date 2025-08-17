package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.CrossCommitteeRequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestDocumentRepository extends JpaRepository<CrossCommitteeRequestDocument, Long> {
    
    // Find all documents for a cross-committee request, ordered by upload date
    List<CrossCommitteeRequestDocument> findByCrossCommitteeRequestIdOrderByUploadedAtDesc(Long crossCommitteeRequestId);
    
    // Delete all documents for a cross-committee request
    void deleteByCrossCommitteeRequestId(Long crossCommitteeRequestId);
}