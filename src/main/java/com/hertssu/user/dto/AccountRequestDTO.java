package com.hertssu.user.dto;

import com.hertssu.model.AccountRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountRequestDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Integer committeeId;
    private String committeeName;
    private Integer subcommitteeId;
    private String subcommitteeName;
    private UUID interviewId;
    private String gafId;
    private String phoneNumber;
    private LocalDateTime requestedAt;
    private String notes;
    
    public AccountRequestDTO(AccountRequest accountRequest) {
        this.id = accountRequest.getId();
        this.firstName = accountRequest.getFirstName();
        this.lastName = accountRequest.getLastName();
        this.email = accountRequest.getEmail();
        this.role = accountRequest.getRole();
        this.committeeId = accountRequest.getCommittee().getId();
        this.committeeName = accountRequest.getCommittee().getName();
        
        if (accountRequest.getSubcommittee() != null) {
            this.subcommitteeId = accountRequest.getSubcommittee().getId();
            this.subcommitteeName = accountRequest.getSubcommittee().getName();
        }
        
        this.interviewId = accountRequest.getInterviewId();
        this.gafId = accountRequest.getGafId();
        this.phoneNumber = accountRequest.getPhoneNumber();
        this.requestedAt = accountRequest.getRequestedAt();
        this.notes = accountRequest.getNotes();
    }
}