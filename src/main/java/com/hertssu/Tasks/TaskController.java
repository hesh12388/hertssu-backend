package com.hertssu.Tasks;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.hertssu.Tasks.dto.*;
import com.hertssu.security.AuthUserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;
    private final DocumentService documentService;

    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
                System.out.println("Creating task: " + request.getTitle());
        TaskResponse response = taskService.createTask(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/assigned-to-me")
    public ResponseEntity<List<TaskResponse>> getTasksAssignedToMe(
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        List<TaskResponse> tasks = taskService.getTasksAssignedToUser(currentUser.getId());
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/assigned-by-me")
    public ResponseEntity<List<TaskResponse>> getTasksAssignedByMe(
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        List<TaskResponse> tasks = taskService.getTasksAssignedByUser(currentUser.getId());
        return ResponseEntity.ok(tasks);
    }

     @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        TaskResponse response = taskService.updateTask(taskId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        TaskResponse response = taskService.updateTaskStatus(taskId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        taskService.deleteTask(taskId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        CommentResponse response = commentService.addComment(taskId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long taskId) {
        List<CommentResponse> comments = commentService.getComments(taskId);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    
    @PostMapping("/{taskId}/documents")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long taskId,
            @Valid @ModelAttribute UploadDocumentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        DocumentResponse response = documentService.uploadDocument(taskId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{taskId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long taskId) {
        List<DocumentResponse> documents = documentService.getDocuments(taskId);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<DocumentDownloadResponse> downloadDocument(
            @PathVariable Long documentId) {
        DocumentDownloadResponse response = documentService.downloadDocument(documentId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}