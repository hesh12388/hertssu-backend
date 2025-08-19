package com.hertssu.Warnings.dto;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningResponse {
    
    private Long id;
    
    private UserSummary assigner;
    
    private UserSummary assignee;
    
    private LocalDateTime issuedDate;
    
    private String reason;
    
    private String actionTaken;
    
    private WarningSeverity severity;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

