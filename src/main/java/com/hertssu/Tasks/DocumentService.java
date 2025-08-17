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
import com.hertssu.Tasks.dto.EntityType;
import com.hertssu.model.*;

import com.hertssu.Proposals.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final TaskDocumentRepository taskDocumentRepository;
    private final ProposalDocumentRepository proposalDocumentRepository;
    private final CrossCommitteeRequestDocumentRepository crossCommitteeRequestDocumentRepository;
    
    private final TaskRepository taskRepository;
    private final ProposalRepository proposalRepository;
    private final CrossCommitteeRequestRepository crossCommitteeRequestRepository;
    
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // Generic Upload Document
    public DocumentResponse uploadDocument(Long entityId, EntityType entityType, UploadDocumentRequest request, Long currentUserId) {
        User user = userRepository.getReferenceById(currentUserId);
        MultipartFile file = request.getFile();
        
        switch (entityType) {
            case TASK:
                return uploadTaskDocument(entityId, file, user);
            case PROPOSAL:
                return uploadProposalDocument(entityId, file, user);
            case CROSS_COMMITTEE_REQUEST:
                return uploadCrossCommitteeRequestDocument(entityId, file, user);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Generic Get Documents
    public List<DocumentResponse> getDocuments(Long entityId, EntityType entityType) {
        switch (entityType) {
            case TASK:
                return getTaskDocuments(entityId);
            case PROPOSAL:
                return getProposalDocuments(entityId);
            case CROSS_COMMITTEE_REQUEST:
                return getCrossCommitteeRequestDocuments(entityId);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Generic Download Document
    public DocumentDownloadResponse downloadDocument(Long documentId, EntityType entityType) {
        String s3Key;
        
        switch (entityType) {
            case TASK:
                TaskDocument taskDocument = taskDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Key = taskDocument.getS3Key();
                break;
            case PROPOSAL:
                ProposalDocument proposalDocument = proposalDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Key = proposalDocument.getS3Key();
                break;
            case CROSS_COMMITTEE_REQUEST:
                CrossCommitteeRequestDocument crossCommitteeRequestDocument = crossCommitteeRequestDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Key = crossCommitteeRequestDocument.getS3Key();
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }

        String presignedUrl = s3Service.generatePresignedDownloadUrl(s3Key, 15);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        
        DocumentDownloadResponse response = new DocumentDownloadResponse();
        response.setDownloadUrl(presignedUrl);
        response.setExpiresAt(expiresAt);
        return response;
    }

    // Generic Delete Document
    public void deleteDocument(Long documentId, EntityType entityType) {
        switch (entityType) {
            case TASK:
                TaskDocument taskDocument = taskDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Service.deleteFile(taskDocument.getS3Key());
                taskDocumentRepository.deleteById(documentId);
                break;
            case PROPOSAL:
                ProposalDocument proposalDocument = proposalDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Service.deleteFile(proposalDocument.getS3Key());
                proposalDocumentRepository.deleteById(documentId);
                break;
            case CROSS_COMMITTEE_REQUEST:
                CrossCommitteeRequestDocument crossCommitteeRequestDocument = crossCommitteeRequestDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
                s3Service.deleteFile(crossCommitteeRequestDocument.getS3Key());
                crossCommitteeRequestDocumentRepository.deleteById(documentId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Private helper methods
    private DocumentResponse uploadTaskDocument(Long taskId, MultipartFile file, User user) {
        Task task = taskRepository.getReferenceById(taskId);
        String keyPrefix = "tasks/" + taskId + "/documents/";
        String s3Key = s3Service.uploadFile(file, keyPrefix);
        
        TaskDocument document = new TaskDocument();
        document.setTask(task);
        document.setUploadedBy(user);
        document.setFileName(file.getOriginalFilename());
        document.setS3Key(s3Key);
        document.setS3Bucket(s3Service.getBucketName());
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        
        TaskDocument savedDocument = taskDocumentRepository.save(document);
        return convertTaskDocumentToResponse(savedDocument);
    }

    private DocumentResponse uploadProposalDocument(Long proposalId, MultipartFile file, User user) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        String keyPrefix = "proposals/" + proposalId + "/documents/";
        String s3Key = s3Service.uploadFile(file, keyPrefix);
        
        ProposalDocument document = new ProposalDocument();
        document.setProposal(proposal);
        document.setUploadedBy(user);
        document.setFileName(file.getOriginalFilename());
        document.setS3Key(s3Key);
        document.setS3Bucket(s3Service.getBucketName());
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        
        ProposalDocument savedDocument = proposalDocumentRepository.save(document);
        return convertProposalDocumentToResponse(savedDocument);
    }

    private DocumentResponse uploadCrossCommitteeRequestDocument(Long requestId, MultipartFile file, User user) {
        CrossCommitteeRequest request = crossCommitteeRequestRepository.getReferenceById(requestId);
        String keyPrefix = "cross-committee-requests/" + requestId + "/documents/";
        String s3Key = s3Service.uploadFile(file, keyPrefix);
        
        CrossCommitteeRequestDocument document = new CrossCommitteeRequestDocument();
        document.setCrossCommitteeRequest(request);
        document.setUploadedBy(user);
        document.setFileName(file.getOriginalFilename());
        document.setS3Key(s3Key);
        document.setS3Bucket(s3Service.getBucketName());
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        
        CrossCommitteeRequestDocument savedDocument = crossCommitteeRequestDocumentRepository.save(document);
        return convertCrossCommitteeRequestDocumentToResponse(savedDocument);
    }

    private List<DocumentResponse> getTaskDocuments(Long taskId) {
        List<TaskDocument> documents = taskDocumentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
        return documents.stream().map(this::convertTaskDocumentToResponse).collect(Collectors.toList());
    }

    private List<DocumentResponse> getProposalDocuments(Long proposalId) {
        List<ProposalDocument> documents = proposalDocumentRepository.findByProposalIdOrderByUploadedAtDesc(proposalId);
        return documents.stream().map(this::convertProposalDocumentToResponse).collect(Collectors.toList());
    }

    private List<DocumentResponse> getCrossCommitteeRequestDocuments(Long requestId) {
        List<CrossCommitteeRequestDocument> documents = crossCommitteeRequestDocumentRepository.findByCrossCommitteeRequestIdOrderByUploadedAtDesc(requestId);
        return documents.stream().map(this::convertCrossCommitteeRequestDocumentToResponse).collect(Collectors.toList());
    }

    // Conversion methods
    private DocumentResponse convertTaskDocumentToResponse(TaskDocument document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFileSize(document.getFileSize());
        response.setContentType(document.getContentType());
        response.setUploadedBy(convertToUserSummary(document.getUploadedBy()));
        response.setUploadedAt(document.getUploadedAt());
        return response;
    }

    private DocumentResponse convertProposalDocumentToResponse(ProposalDocument document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFileSize(document.getFileSize());
        response.setContentType(document.getContentType());
        response.setUploadedBy(convertToUserSummary(document.getUploadedBy()));
        response.setUploadedAt(document.getUploadedAt());
        return response;
    }

    private DocumentResponse convertCrossCommitteeRequestDocumentToResponse(CrossCommitteeRequestDocument document) {
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