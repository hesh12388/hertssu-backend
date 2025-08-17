package com.hertssu.Proposals.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossCommitteeRequestResponse {
    private Long id;
    private Long proposalId;
    private String title;
    private String description;
    private CommitteeSummaryResponse requester;
    private CommitteeSummaryResponse targetCommittee;
    private CrossCommitteeRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}