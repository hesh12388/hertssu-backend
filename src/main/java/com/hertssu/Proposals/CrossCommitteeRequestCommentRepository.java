package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.CrossCommitteeRequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestCommentRepository extends JpaRepository<CrossCommitteeRequestComment, Long> {
    
    List<CrossCommitteeRequestComment> findByCrossCommitteeRequestIdOrderByCreatedAtAsc(Long crossCommitteeRequestId);
    
    void deleteByCrossCommitteeRequestId(Long crossCommitteeRequestId);
}