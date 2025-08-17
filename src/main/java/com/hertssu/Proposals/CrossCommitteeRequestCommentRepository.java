package com.hertssu.Proposals;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.CrossCommitteeRequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CrossCommitteeRequestCommentRepository extends JpaRepository<CrossCommitteeRequestComment, Long> {
    
    // Find all comments for a cross-committee request, ordered by creation date
    List<CrossCommitteeRequestComment> findByCrossCommitteeRequestIdOrderByCreatedAtAsc(Long crossCommitteeRequestId);
    
    // Delete all comments for a cross-committee request (useful when deleting a request)
    void deleteByCrossCommitteeRequestId(Long crossCommitteeRequestId);
}