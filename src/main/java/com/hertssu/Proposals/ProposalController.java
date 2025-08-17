package com.hertssu.Proposals;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.hertssu.Proposals.dto.*;
import com.hertssu.Tasks.CommentService;
import com.hertssu.Tasks.DocumentService;
import com.hertssu.Tasks.dto.CommentResponse;
import com.hertssu.Tasks.dto.CreateCommentRequest;
import com.hertssu.Tasks.dto.DocumentDownloadResponse;
import com.hertssu.Tasks.dto.DocumentResponse;
import com.hertssu.Tasks.dto.UploadDocumentRequest;
import com.hertssu.security.AuthUserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hertssu.Tasks.dto.EntityType;
@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;
    private final CommentService commentService;
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(
            @Valid @RequestBody CreateProposalRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        ProposalResponse response = proposalService.createProposal(request, currentUser.getCommitteeId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-proposals")
    public ResponseEntity<List<ProposalResponse>> getMyProposals(
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
       
        List<ProposalResponse> proposals = proposalService.getMyProposals(currentUser.getCommitteeId(), currentUser.getSubcommitteeId(), currentUser.getRole());
        return ResponseEntity.ok(proposals);
    }

   @GetMapping("/all")
    public ResponseEntity<List<ProposalResponse>> getAllProposals() {
        List<ProposalResponse> proposals = proposalService.getAllProposals();
        return ResponseEntity.ok(proposals);
    }

    @PutMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> updateProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody UpdateProposalRequest request) {
        ProposalResponse response = proposalService.updateProposal(proposalId, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{proposalId}/status")
    public ResponseEntity<ProposalResponse> updateProposalStatus(
            @PathVariable Long proposalId,
            @Valid @RequestBody UpdateProposalStatusRequest request) {
        ProposalResponse response = proposalService.updateProposalStatus(proposalId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{proposalId}")
    public ResponseEntity<Void> deleteProposal(
            @PathVariable Long proposalId) {
        proposalService.deleteProposal(proposalId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{proposalId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long proposalId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        CommentResponse response = commentService.addComment(proposalId, EntityType.PROPOSAL, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{proposalId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long proposalId) {
        List<CommentResponse> comments = commentService.getComments(proposalId, EntityType.PROPOSAL);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, EntityType.PROPOSAL);
        return ResponseEntity.noContent().build();
    }

  
    @PostMapping("/{proposalId}/documents")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long proposalId,
            @Valid @ModelAttribute UploadDocumentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        DocumentResponse response = documentService.uploadDocument(proposalId, EntityType.PROPOSAL, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{proposalId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long proposalId) {
        List<DocumentResponse> documents = documentService.getDocuments(proposalId, EntityType.PROPOSAL);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<DocumentDownloadResponse> downloadDocument(@PathVariable Long documentId) {
        DocumentDownloadResponse response = documentService.downloadDocument(documentId, EntityType.PROPOSAL);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId, EntityType.PROPOSAL);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{proposalId}/cross-committee-requests")
    public ResponseEntity<CrossCommitteeRequestResponse> createCrossCommitteeRequest(
            @PathVariable Long proposalId,
            @Valid @RequestBody CreateCrossCommitteeRequestRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        CrossCommitteeRequestResponse response = proposalService.createCrossCommitteeRequest(proposalId, request, currentUser.getSubcommitteeId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cross-committee-requests/for-my-committee")
    public ResponseEntity<List<CrossCommitteeRequestResponse>> getCrossCommitteeRequestsForMyCommittee(
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        List<CrossCommitteeRequestResponse> requests = proposalService.getCrossCommitteeRequestsForCommittee(currentUser.getCommitteeId());
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/{proposalId}/cross-committee-requests")
    public ResponseEntity<List<CrossCommitteeRequestResponse>> getCrossCommitteeRequestsForProposal(
            @PathVariable Long proposalId) {
        List<CrossCommitteeRequestResponse> requests = proposalService.getCrossCommitteeRequestsForProposal(proposalId);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/cross-committee-requests/{requestId}/status")
    public ResponseEntity<CrossCommitteeRequestResponse> updateCrossCommitteeRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody UpdateCrossCommitteeRequestStatusRequest request) {
        CrossCommitteeRequestResponse response = proposalService.updateCrossCommitteeRequestStatus(requestId, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/cross-committee-requests/{requestId}/comments")
    public ResponseEntity<CommentResponse> addCrossCommitteeRequestComment(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        CommentResponse response = commentService.addComment(requestId, EntityType.CROSS_COMMITTEE_REQUEST, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cross-committee-requests/{requestId}/comments")
    public ResponseEntity<List<CommentResponse>> getCrossCommitteeRequestComments(
            @PathVariable Long requestId) {
        List<CommentResponse> comments = commentService.getComments(requestId, EntityType.CROSS_COMMITTEE_REQUEST);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/cross-committee-requests/comments/{commentId}")
    public ResponseEntity<Void> deleteCrossCommitteeRequestComment(
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId, EntityType.CROSS_COMMITTEE_REQUEST);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cross-committee-requests/{requestId}/documents")
    public ResponseEntity<DocumentResponse> uploadCrossCommitteeRequestDocument(
            @PathVariable Long requestId,
            @Valid @ModelAttribute UploadDocumentRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        DocumentResponse response = documentService.uploadDocument(requestId, EntityType.CROSS_COMMITTEE_REQUEST, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cross-committee-requests/{requestId}/documents")
    public ResponseEntity<List<DocumentResponse>> getCrossCommitteeRequestDocuments(
            @PathVariable Long requestId) {
        List<DocumentResponse> documents = documentService.getDocuments(requestId, EntityType.CROSS_COMMITTEE_REQUEST);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/cross-committee-requests/documents/{documentId}/download")
    public ResponseEntity<DocumentDownloadResponse> downloadCrossCommitteeRequestDocument(
            @PathVariable Long documentId) {
        DocumentDownloadResponse response = documentService.downloadDocument(documentId, EntityType.CROSS_COMMITTEE_REQUEST);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/cross-committee-requests/documents/{documentId}")
    public ResponseEntity<Void> deleteCrossCommitteeRequestDocument(
            @PathVariable Long documentId) {
        documentService.deleteDocument(documentId, EntityType.CROSS_COMMITTEE_REQUEST);
        return ResponseEntity.noContent().build();
    }
}