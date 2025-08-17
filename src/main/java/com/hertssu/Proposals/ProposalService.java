package com.hertssu.Proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.hertssu.Committee.CommitteeRepository;
import com.hertssu.Proposals.dto.*;
import com.hertssu.Subcommittee.SubcommitteeRepository;
import com.hertssu.model.Proposal;
import com.hertssu.model.Subcommittee;
import com.hertssu.model.Committee;
import com.hertssu.model.CrossCommitteeRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final CrossCommitteeRequestRepository crossCommitteeRequestRepository;
    private final SubcommitteeRepository subcommitteeRepository;
    private final CommitteeRepository committeeRepository;

    // Create Proposal
    public ProposalResponse createProposal(CreateProposalRequest request, int assignerId) {
        
       
        Committee assigner = committeeRepository.getReferenceById(assignerId);
        
       
        Subcommittee assignee = subcommitteeRepository.getReferenceById(request.getAssigneeId());
        
        // Create the proposal entity
        Proposal proposal = new Proposal();
        proposal.setTitle(request.getTitle());
        proposal.setDescription(request.getDescription());
        proposal.setAssigner(assigner);
        proposal.setAssignee(assignee);
        proposal.setDueDate(request.getDueDate());
        proposal.setPriority(request.getPriority());
        proposal.setStatus(ProposalStatus.IN_PROGRESS);
        
        // Save to database
        Proposal savedProposal = proposalRepository.save(proposal);
        
        // Convert to response DTO and return
        return convertToProposalResponse(savedProposal);
    }

    // Get My Proposals
    public List<ProposalResponse> getMyProposals(Integer committeeId, Integer subcommitteeId, String userRole) {
        System.out.println("Fetching proposals for committeeId: " + committeeId + ", subcommitteeId: " + subcommitteeId + ", userRole: " + userRole);
        if (userRole.equals("Chairperson") || userRole.equals("Associate Chairperson")) {
            try{
                // Get proposals assigned BY this committee
                Committee committee = committeeRepository.getReferenceById(committeeId);
                List<Proposal> proposals = proposalRepository.findByAssigner(committee);
                return proposals.stream().map(this::convertToProposalResponse).collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("Error fetching proposals for committee: " + e.getMessage());
                return new ArrayList<>();
            }
        } 
        else if (userRole.equals("Leader") || userRole.equals("Associate Leader")) {
            // Get proposals assigned TO this subcommittee
            Subcommittee subcommittee = subcommitteeRepository.getReferenceById(subcommitteeId);
            List<Proposal> proposals = proposalRepository.findByAssignee(subcommittee);
            return proposals.stream().map(this::convertToProposalResponse).collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

     // Get All Proposals
    public List<ProposalResponse> getAllProposals() {
    
        List<Proposal> allProposals = proposalRepository.findAll();
        
        return allProposals.stream()
                .map(this::convertToProposalResponse)
                .collect(Collectors.toList());
    }

    // Update Proposal
    public ProposalResponse updateProposal(Long proposalId, UpdateProposalRequest request) {
        // Find the proposal
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
            
        // Update the proposal fields
        proposal.setTitle(request.getTitle());
        proposal.setDescription(request.getDescription());
        proposal.setDueDate(request.getDueDate());
        proposal.setPriority(request.getPriority());
        
        // Save the updated proposal
        Proposal updatedProposal = proposalRepository.save(proposal);
        
        // Convert to response DTO and return
        return convertToProposalResponse(updatedProposal);
    }

    // Update Proposal Status
    public ProposalResponse updateProposalStatus(Long proposalId, UpdateProposalStatusRequest request) {
        // Find the proposal
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        
        ProposalStatus newStatus = request.getStatus();
        
        // Update the status
        proposal.setStatus(newStatus);
        
        // If proposal is completed, mark all cross-committee requests as completed too
        if (newStatus == ProposalStatus.COMPLETED) {
            List<CrossCommitteeRequest> crossCommitteeRequests = crossCommitteeRequestRepository.findByProposalIdOrderByCreatedAtDesc(proposalId);
            for (CrossCommitteeRequest crossRequest : crossCommitteeRequests) {
                crossRequest.setStatus(CrossCommitteeRequestStatus.COMPLETED);
                crossCommitteeRequestRepository.save(crossRequest);
            }
        }
        
        // Save the updated proposal
        Proposal updatedProposal = proposalRepository.save(proposal);
        
        // Convert to response DTO and return
        return convertToProposalResponse(updatedProposal);
    }

    // Delete Proposal
    public void deleteProposal(Long proposalId) {
          
        // Delete the proposal itself - cascade will handle comments, documents, and cross-committee requests
        proposalRepository.deleteById(proposalId);
    }

    // Create Cross-Committee Request
    public CrossCommitteeRequestResponse createCrossCommitteeRequest(Long proposalId, CreateCrossCommitteeRequestRequest request, int requesterId) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        
        Subcommittee requester = subcommitteeRepository.getReferenceById(requesterId);

        Committee targetCommittee = committeeRepository.getReferenceById(request.getTargetCommitteeId());
        
        // Create the cross-committee request entity
        CrossCommitteeRequest crossCommitteeRequest = new CrossCommitteeRequest();
        crossCommitteeRequest.setProposal(proposal);
        crossCommitteeRequest.setTitle(request.getTitle());
        crossCommitteeRequest.setDescription(request.getDescription());
        crossCommitteeRequest.setRequester(requester);
        crossCommitteeRequest.setTargetCommittee(targetCommittee);
        crossCommitteeRequest.setStatus(CrossCommitteeRequestStatus.IN_PROGRESS);
        
        // Save to database
        CrossCommitteeRequest savedRequest = crossCommitteeRequestRepository.save(crossCommitteeRequest);
        
        // Convert to response DTO and return
        return convertToCrossCommitteeRequestResponse(savedRequest);
    }
    // Get Cross-Committee Requests
    public List<CrossCommitteeRequestResponse> getCrossCommitteeRequestsForCommittee(int committeeId) {
        
        Committee committee = committeeRepository.getReferenceById(committeeId);
       
    
        // Fetch cross-committee requests targeting this user's committee
        List<CrossCommitteeRequest> requests = crossCommitteeRequestRepository
            .findByTargetCommitteeOrderByCreatedAtDesc(committee);
        
        // Convert to response DTOs
        return requests.stream()
                .map(this::convertToCrossCommitteeRequestResponse)
                .collect(Collectors.toList());
    }

    // Get Cross-Committee Requests for Proposal
    public List<CrossCommitteeRequestResponse> getCrossCommitteeRequestsForProposal(Long proposalId) {
        // Fetch all cross-committee requests for this proposal
        List<CrossCommitteeRequest> requests = crossCommitteeRequestRepository.findByProposalIdOrderByCreatedAtDesc(proposalId);
        
        // Convert to response DTOs
        return requests.stream()
                .map(this::convertToCrossCommitteeRequestResponse)
                .collect(Collectors.toList());
    }

    // Update Cross-Committee Request Status
    public CrossCommitteeRequestResponse updateCrossCommitteeRequestStatus(Long requestId, UpdateCrossCommitteeRequestStatusRequest request) {
        // Find the cross-committee request
        CrossCommitteeRequest crossCommitteeRequest = crossCommitteeRequestRepository.getReferenceById(requestId);
        
        CrossCommitteeRequestStatus newStatus = request.getStatus();
        
        // Update the status
        crossCommitteeRequest.setStatus(newStatus);
        
        // Save the updated request
        CrossCommitteeRequest updatedRequest = crossCommitteeRequestRepository.save(crossCommitteeRequest);
        
        // Convert to response DTO and return
        return convertToCrossCommitteeRequestResponse(updatedRequest);
    }

    // Convert proposal to ProposalResponse
    private ProposalResponse convertToProposalResponse(Proposal proposal) {
        ProposalResponse response = new ProposalResponse();
        response.setId(proposal.getId());
        response.setTitle(proposal.getTitle());
        response.setDescription(proposal.getDescription());
        response.setAssigner(convertToCommitteeSummary(proposal.getAssigner()));
        response.setAssignee(convertToSubcommitteeSummary(proposal.getAssignee()));
        response.setDueDate(proposal.getDueDate());
        response.setPriority(proposal.getPriority());
        response.setStatus(proposal.getStatus());
        response.setCreatedAt(proposal.getCreatedAt());
        response.setUpdatedAt(proposal.getUpdatedAt());
        return response;
    }

    // Convert cross-committee request to CrossCommitteeRequestResponse
    private CrossCommitteeRequestResponse convertToCrossCommitteeRequestResponse(CrossCommitteeRequest request) {
        CrossCommitteeRequestResponse response = new CrossCommitteeRequestResponse();
        response.setId(request.getId());
        response.setProposalId(request.getProposal().getId());
        response.setTitle(request.getTitle());
        response.setDescription(request.getDescription());
        response.setRequester(convertToSubcommitteeSummary(request.getRequester()));
        response.setTargetCommittee(convertToCommitteeSummary(request.getTargetCommittee()));
        response.setStatus(request.getStatus());
        response.setCreatedAt(request.getCreatedAt());
        response.setUpdatedAt(request.getUpdatedAt());
        return response;
    }

    private CommitteeSummaryResponse convertToCommitteeSummary(com.hertssu.model.Committee committee) {
        CommitteeSummaryResponse response = new CommitteeSummaryResponse();
        response.setId(committee.getId());
        response.setCommitteeName(committee.getName());
        response.setCommiteeSlug(committee.getSlug());
        return response;
    }

    private CommitteeSummaryResponse convertToSubcommitteeSummary(Subcommittee committee) {
        CommitteeSummaryResponse response = new CommitteeSummaryResponse();
        response.setId(committee.getId());
        response.setCommitteeName(committee.getName());
        response.setCommiteeSlug(committee.getSlug());
        return response;
    }

}