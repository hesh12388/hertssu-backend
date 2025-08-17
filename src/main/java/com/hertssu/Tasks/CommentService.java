package com.hertssu.Tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.hertssu.Tasks.dto.CreateCommentRequest;
import com.hertssu.Tasks.dto.UserSummaryResponse;
import com.hertssu.Tasks.dto.CommentResponse;
import com.hertssu.Tasks.dto.EntityType;
import com.hertssu.model.*;
import com.hertssu.Proposals.*;
import com.hertssu.user.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final ProposalCommentRepository proposalCommentRepository;
    private final CrossCommitteeRequestCommentRepository crossCommitteeRequestCommentRepository;
    
    private final TaskRepository taskRepository;
    private final ProposalRepository proposalRepository;
    private final CrossCommitteeRequestRepository crossCommitteeRequestRepository;
    
    private final UserRepository userRepository;

    // Generic Add Comment
    public CommentResponse addComment(Long entityId, EntityType entityType, CreateCommentRequest request, Long currentUserId) {
        User user = userRepository.getReferenceById(currentUserId);
        
        switch (entityType) {
            case TASK:
                return addTaskComment(entityId, request, user);
            case PROPOSAL:
                return addProposalComment(entityId, request, user);
            case CROSS_COMMITTEE_REQUEST:
                return addCrossCommitteeRequestComment(entityId, request, user);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Generic Get Comments
    public List<CommentResponse> getComments(Long entityId, EntityType entityType) {
        switch (entityType) {
            case TASK:
                return getTaskComments(entityId);
            case PROPOSAL:
                return getProposalComments(entityId);
            case CROSS_COMMITTEE_REQUEST:
                return getCrossCommitteeRequestComments(entityId);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Generic Delete Comment
    public void deleteComment(Long commentId, EntityType entityType) {
        switch (entityType) {
            case TASK:
                taskCommentRepository.deleteById(commentId);
                break;
            case PROPOSAL:
                proposalCommentRepository.deleteById(commentId);
                break;
            case CROSS_COMMITTEE_REQUEST:
                crossCommitteeRequestCommentRepository.deleteById(commentId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // Private methods for specific entity types
    private CommentResponse addTaskComment(Long taskId, CreateCommentRequest request, User user) {
        Task task = taskRepository.getReferenceById(taskId);
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());
        TaskComment savedComment = taskCommentRepository.save(comment);
        return convertTaskCommentToResponse(savedComment);
    }

    private CommentResponse addProposalComment(Long proposalId, CreateCommentRequest request, User user) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        ProposalComment comment = new ProposalComment();
        comment.setProposal(proposal);
        comment.setUser(user);
        comment.setContent(request.getContent());
        ProposalComment savedComment = proposalCommentRepository.save(comment);
        return convertProposalCommentToResponse(savedComment);
    }

    private CommentResponse addCrossCommitteeRequestComment(Long requestId, CreateCommentRequest request, User user) {
        CrossCommitteeRequest crossCommitteeRequest = crossCommitteeRequestRepository.getReferenceById(requestId);
        CrossCommitteeRequestComment comment = new CrossCommitteeRequestComment();
        comment.setCrossCommitteeRequest(crossCommitteeRequest);
        comment.setUser(user);
        comment.setContent(request.getContent());
        CrossCommitteeRequestComment savedComment = crossCommitteeRequestCommentRepository.save(comment);
        return convertCrossCommitteeRequestCommentToResponse(savedComment);
    }

    private List<CommentResponse> getTaskComments(Long taskId) {
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return comments.stream().map(this::convertTaskCommentToResponse).collect(Collectors.toList());
    }

    private List<CommentResponse> getProposalComments(Long proposalId) {
        List<ProposalComment> comments = proposalCommentRepository.findByProposalIdOrderByCreatedAtAsc(proposalId);
        return comments.stream().map(this::convertProposalCommentToResponse).collect(Collectors.toList());
    }

    private List<CommentResponse> getCrossCommitteeRequestComments(Long requestId) {
        List<CrossCommitteeRequestComment> comments = crossCommitteeRequestCommentRepository.findByCrossCommitteeRequestIdOrderByCreatedAtAsc(requestId);
        return comments.stream().map(this::convertCrossCommitteeRequestCommentToResponse).collect(Collectors.toList());
    }

    // Conversion methods
    private CommentResponse convertTaskCommentToResponse(TaskComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUser(convertToUserSummary(comment.getUser()));
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private CommentResponse convertProposalCommentToResponse(ProposalComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUser(convertToUserSummary(comment.getUser()));
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private CommentResponse convertCrossCommitteeRequestCommentToResponse(CrossCommitteeRequestComment comment) {
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