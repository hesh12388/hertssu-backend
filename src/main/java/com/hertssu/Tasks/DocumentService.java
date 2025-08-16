package com.hertssu.Tasks;

import com.hertssu.utils.S3Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.hertssu.user.UserRepository;
import com.hertssu.Tasks.dto.*;
import com.hertssu.model.Task;
import com.hertssu.model.TaskDocument;
import com.hertssu.model.User;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final TaskDocumentRepository taskDocumentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // Upload Document
    public DocumentResponse uploadDocument(Long taskId, UploadDocumentRequest request, Long currentUserId) {
        // Get references
        Task task = taskRepository.getReferenceById(taskId);
        User user = userRepository.getReferenceById(currentUserId);
        
        MultipartFile file = request.getFile();
        
        // Upload file to S3
        String keyPrefix = "tasks/" + taskId + "/documents/";
        String s3Key = s3Service.uploadFile(file, keyPrefix);
        String s3Bucket = s3Service.getBucketName();
        
        // Create document record
        TaskDocument document = new TaskDocument();
        document.setTask(task);
        document.setUploadedBy(user);
        document.setFileName(file.getOriginalFilename());
        document.setS3Key(s3Key);
        document.setS3Bucket(s3Bucket);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        
        // Save to database
        TaskDocument savedDocument = taskDocumentRepository.save(document);
        
        // Convert to response DTO and return
        return convertToDocumentResponse(savedDocument);
    }

    // Get Documents for Task
    public List<DocumentResponse> getDocuments(Long taskId) {
        // Fetch all documents for this task
        List<TaskDocument> documents = taskDocumentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
        
        // Convert to response DTOs
        return documents.stream()
                .map(this::convertToDocumentResponse)
                .collect(Collectors.toList());
    }

    // Download Document 
    public DocumentDownloadResponse downloadDocument(Long documentId) {
        // Get the document
        TaskDocument document = taskDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        // Generate presigned URL (15 minutes expiration)
        String presignedUrl = s3Service.generatePresignedDownloadUrl(document.getS3Key(), 15);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        
        DocumentDownloadResponse response = new DocumentDownloadResponse();
        response.setDownloadUrl(presignedUrl);
        response.setExpiresAt(expiresAt);
        
        return response;
    }

    // Delete Document
    public void deleteDocument(Long documentId) {
        // Get the document to get S3 info before deleting
        TaskDocument document = taskDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        // Delete file from S3
        s3Service.deleteFile(document.getS3Key());
        
        // Delete document record from database
        taskDocumentRepository.deleteById(documentId);
    }

    // Helper method for converting entity to DTO
    private DocumentResponse convertToDocumentResponse(TaskDocument document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFileSize(document.getFileSize());
        response.setContentType(document.getContentType());
        response.setUploadedBy(convertToUserSummary(document.getUploadedBy()));
        response.setUploadedAt(document.getUploadedAt());
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