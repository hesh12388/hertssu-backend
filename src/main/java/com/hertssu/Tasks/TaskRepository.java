package com.hertssu.Tasks;
import org.springframework.stereotype.Repository;
import com.hertssu.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks assigned to a user
    List<Task> findByAssigneeId(Long assigneeId);
    
    // Find tasks assigned by a user
    List<Task> findByAssignerId(Long assignerId); 
}
