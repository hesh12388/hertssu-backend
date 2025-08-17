package com.hertssu.Proposals.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hertssu.Tasks.dto.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {
    private Long id;
    private String title;
    private String description;
    private CommitteeSummaryResponse assignee;
    private CommitteeSummaryResponse assigner;
    private LocalDateTime startDate;
    private LocalDate dueDate;
    private Priority priority;
    private ProposalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}