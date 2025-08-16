package com.hertssu.Tasks;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.TaskDocument;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface TaskDocumentRepository extends JpaRepository<TaskDocument, Long> {
    
    // Find all documents for a task, ordered by upload date
    List<TaskDocument> findByTaskIdOrderByUploadedAtDesc(Long taskId);
    
    // Delete all documents for a task (useful when deleting a task)
    void deleteByTaskId(Long taskId);

}