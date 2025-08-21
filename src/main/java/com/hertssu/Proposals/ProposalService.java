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

    public ProposalResponse createProposal(CreateProposalRequest request, int assignerId) {
        
       
        Committee assigner = committeeRepository.getReferenceById(assignerId);
        
       
        Subcommittee assignee = subcommitteeRepository.getReferenceById(request.getAssigneeId());
        
        Proposal proposal = new Proposal();
        proposal.setTitle(request.getTitle());
        proposal.setDescription(request.getDescription());
        proposal.setAssigner(assigner);
        proposal.setAssignee(assignee);
        proposal.setDueDate(request.getDueDate());
        proposal.setPriority(request.getPriority());
        proposal.setStatus(ProposalStatus.IN_PROGRESS);
        
        Proposal savedProposal = proposalRepository.save(proposal);
        
        return convertToProposalResponse(savedProposal);
    }

    public List<ProposalResponse> getMyProposals(Integer committeeId, Integer subcommitteeId, String userRole) {
        System.out.println("Fetching proposals for committeeId: " + committeeId + ", subcommitteeId: " + subcommitteeId + ", userRole: " + userRole);
        if (userRole.equals("CHAIRPERSON") || userRole.equals("ASSOCIATE_CHAIRPERSON")) {
            try{
                Committee committee = committeeRepository.getReferenceById(committeeId);
                List<Proposal> proposals = proposalRepository.findByAssigner(committee);
                return proposals.stream().map(this::convertToProposalResponse).collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("Error fetching proposals for committee: " + e.getMessage());
                return new ArrayList<>();
            }
        } 
        else if (userRole.equals("LEADER") || userRole.equals("ASSOCIATE_LEADER")) {
            Subcommittee subcommittee = subcommitteeRepository.getReferenceById(subcommitteeId);
            List<Proposal> proposals = proposalRepository.findByAssignee(subcommittee);
            return proposals.stream().map(this::convertToProposalResponse).collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    public List<ProposalResponse> getAllProposals() {
    
        List<Proposal> allProposals = proposalRepository.findAll();
        
        return allProposals.stream()
                .map(this::convertToProposalResponse)
                .collect(Collectors.toList());
    }

    public ProposalResponse updateProposal(Long proposalId, UpdateProposalRequest request) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
            
        proposal.setTitle(request.getTitle());
        proposal.setDescription(request.getDescription());
        proposal.setDueDate(request.getDueDate());
        proposal.setPriority(request.getPriority());
        
        Proposal updatedProposal = proposalRepository.save(proposal);
        
        return convertToProposalResponse(updatedProposal);
    }

    public ProposalResponse updateProposalStatus(Long proposalId, UpdateProposalStatusRequest request) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        
        ProposalStatus newStatus = request.getStatus();
        
        proposal.setStatus(newStatus);
        
        List<CrossCommitteeRequest> crossCommitteeRequests = crossCommitteeRequestRepository.findByProposalIdOrderByCreatedAtDesc(proposalId);
        for (CrossCommitteeRequest crossRequest : crossCommitteeRequests) {
            CrossCommitteeRequestStatus crossRequestStatus;
            switch (newStatus) {
                case IN_PROGRESS:
                    crossRequestStatus = CrossCommitteeRequestStatus.IN_PROGRESS;
                    break;
                case PENDING_REVIEW:
                    crossRequestStatus = CrossCommitteeRequestStatus.PENDING_REVIEW;
                    break;
                case COMPLETED:
                    crossRequestStatus = CrossCommitteeRequestStatus.COMPLETED;
                    break;
                default:
                    crossRequestStatus = CrossCommitteeRequestStatus.IN_PROGRESS;
                    break;
            }
            crossRequest.setStatus(crossRequestStatus);
            crossCommitteeRequestRepository.save(crossRequest);
        }
        
        Proposal updatedProposal = proposalRepository.save(proposal);
        
        return convertToProposalResponse(updatedProposal);
    }

    public void deleteProposal(Long proposalId) {
          
        proposalRepository.deleteById(proposalId);
    }

    public CrossCommitteeRequestResponse createCrossCommitteeRequest(Long proposalId, CreateCrossCommitteeRequestRequest request, int requesterId) {
        Proposal proposal = proposalRepository.getReferenceById(proposalId);
        
        Subcommittee requester = subcommitteeRepository.getReferenceById(requesterId);

        Committee targetCommittee = committeeRepository.getReferenceById(request.getTargetCommitteeId());
        
        CrossCommitteeRequest crossCommitteeRequest = new CrossCommitteeRequest();
        crossCommitteeRequest.setProposal(proposal);
        crossCommitteeRequest.setTitle(request.getTitle());
        crossCommitteeRequest.setDescription(request.getDescription());
        crossCommitteeRequest.setRequester(requester);
        crossCommitteeRequest.setTargetCommittee(targetCommittee);
        crossCommitteeRequest.setStatus(CrossCommitteeRequestStatus.IN_PROGRESS);
        
        CrossCommitteeRequest savedRequest = crossCommitteeRequestRepository.save(crossCommitteeRequest);
        
        return convertToCrossCommitteeRequestResponse(savedRequest);
    }

    public List<CrossCommitteeRequestResponse> getCrossCommitteeRequestsForCommittee(int committeeId) {
        
        Committee committee = committeeRepository.getReferenceById(committeeId);
       
        List<CrossCommitteeRequest> requests = crossCommitteeRequestRepository
            .findByTargetCommitteeOrderByCreatedAtDesc(committee);
        
        return requests.stream()
                .map(this::convertToCrossCommitteeRequestResponse)
                .collect(Collectors.toList());
    }

    public List<CrossCommitteeRequestResponse> getCrossCommitteeRequestsForProposal(Long proposalId) {
        List<CrossCommitteeRequest> requests = crossCommitteeRequestRepository.findByProposalIdOrderByCreatedAtDesc(proposalId);
        
        return requests.stream()
                .map(this::convertToCrossCommitteeRequestResponse)
                .collect(Collectors.toList());
    }

    public CrossCommitteeRequestResponse updateCrossCommitteeRequestStatus(Long requestId, UpdateCrossCommitteeRequestStatusRequest request) {
        CrossCommitteeRequest crossCommitteeRequest = crossCommitteeRequestRepository.getReferenceById(requestId);
        
        CrossCommitteeRequestStatus newStatus = request.getStatus();
        
        crossCommitteeRequest.setStatus(newStatus);
        
        CrossCommitteeRequest updatedRequest = crossCommitteeRequestRepository.save(crossCommitteeRequest);
        
        return convertToCrossCommitteeRequestResponse(updatedRequest);
    }

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