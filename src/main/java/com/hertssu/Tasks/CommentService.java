package com.hertssu.Tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.hertssu.Tasks.dto.CreateCommentRequest;
import com.hertssu.Tasks.dto.UserSummaryResponse;
import com.hertssu.Tasks.dto.CommentResponse;
import com.hertssu.model.TaskComment;
import com.hertssu.model.Task;
import com.hertssu.model.User;

import com.hertssu.user.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    // Add Comment to Task
    public CommentResponse addComment(Long taskId, CreateCommentRequest request, Long currentUserId) {
        // Find the task
        Task task = taskRepository.getReferenceById(taskId);
        
        // Find the current user
        User user = userRepository.getReferenceById(currentUserId);
        
        // Create the comment
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        // Save to database
        TaskComment savedComment = taskCommentRepository.save(comment);
        
        // Convert to response DTO and return
        return convertToCommentResponse(savedComment);
    }

    // Get All Comments for a Task
    public List<CommentResponse> getComments(Long taskId) {
       
        // Fetch all comments for this task
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        
        // Convert to response DTOs
        return comments.stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
    }

    // Delete Comment
    public void deleteComment( Long commentId) {
        // Delete the comment
        taskCommentRepository.deleteById(commentId);
    }

    // method for converting comment to response DTO
    private CommentResponse convertToCommentResponse(TaskComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUser(convertToUserSummary(comment.getUser()));
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private UserSummaryResponse convertToUserSummary(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        return response;
    }
}