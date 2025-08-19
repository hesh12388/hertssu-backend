package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.CrossCommitteeRequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestDocumentRepository extends JpaRepository<CrossCommitteeRequestDocument, Long> {
    
    List<CrossCommitteeRequestDocument> findByCrossCommitteeRequestIdOrderByUploadedAtDesc(Long crossCommitteeRequestId);
    
    void deleteByCrossCommitteeRequestId(Long crossCommitteeRequestId);
}