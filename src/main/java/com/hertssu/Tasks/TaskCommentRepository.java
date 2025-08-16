package com.hertssu.Tasks;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.hertssu.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    
    // Find all comments for a task, ordered by creation date
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    
    // Delete all comments for a task (useful when deleting a task)
    void deleteByTaskId(Long taskId);
}
