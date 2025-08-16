package com.hertssu.Tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.hertssu.user.UserRepository;
import com.hertssu.Tasks.dto.*;
import com.hertssu.model.Task;

import com.hertssu.model.User;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskDocumentRepository taskDocumentRepository;
    private final UserRepository userRepository;
    // Create Task
    public TaskResponse createTask(CreateTaskRequest request, Long assignerId) {
        // Validate that the assigner exists
        User assigner = userRepository.getReferenceById(assignerId);
        
        // Validate that the assignee exists
        User assignee = userRepository.getReferenceById(request.getAssigneeId());
        
        // Create the task entity
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigner(assigner);
        task.setAssignee(assignee);
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setStatus(TaskStatus.IN_PROGRESS);
        
        // Save to database
        Task savedTask = taskRepository.save(task);
        
        // Convert to response DTO and return
        return convertToTaskResponse(savedTask);
    }

    // Get Tasks Assigned To User
    public List<TaskResponse> getTasksAssignedToUser(Long userId) {
        // Fetch all tasks assigned to this user
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        
        // Convert each task entity to TaskResponse DTO
        return tasks.stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());
    }


    // Get Tasks Assigned By User
    public List<TaskResponse> getTasksAssignedByUser(Long userId) {
        // Fetch all tasks assigned by this user
        List<Task> tasks = taskRepository.findByAssignerId(userId);
        
        // Convert each task entity to TaskResponse DTO
        return tasks.stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());
    }


    // Update Task
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long currentUserId) {
        // Find the task
        Task task = taskRepository.getReferenceById(taskId);
            
        // Update the task fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        
        // Save the updated task
        Task updatedTask = taskRepository.save(task);
        
        // Convert to response DTO and return
        return convertToTaskResponse(updatedTask);
    }

    // Update Task Status
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, Long currentUserId) {
        // Find the task
        Task task = taskRepository.getReferenceById(taskId);
        
        TaskStatus newStatus = request.getStatus();
        
        // Update the status
        task.setStatus(newStatus);
        
        // Save the updated task
        Task updatedTask = taskRepository.save(task);
        
        // Convert to response DTO and return
        return convertToTaskResponse(updatedTask);
    }


    // Delete Task
    public void deleteTask(Long taskId, Long currentUserId) {
        
        // Delete all comments for this task
        taskCommentRepository.deleteByTaskId(taskId);
        
        // Delete all documents for this task
        taskDocumentRepository.deleteByTaskId(taskId);
        
        // Delete the task itself
        taskRepository.deleteById(taskId);
    }

    // converts task to TaskResponse
    private TaskResponse convertToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setAssigner(convertToUserSummary(task.getAssigner()));
        response.setAssignee(convertToUserSummary(task.getAssignee()));
        response.setDueDate(task.getDueDate());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
                
        return response;
    }

    private UserSummaryResponse convertToUserSummary(User user) {
        UserSummaryResponse summary = new UserSummaryResponse();
        summary.setId(user.getId());
        summary.setName(user.getFirstName() + " " + user.getLastName());
        summary.setEmail(user.getEmail());
        return summary;
    }
}