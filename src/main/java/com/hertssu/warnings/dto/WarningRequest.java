package com.hertssu.Warnings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningRequest {
    
    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
    
    @Size(max = 500, message = "Action taken cannot exceed 500 characters")
    private String actionTaken;
    
    private WarningSeverity severity;
}
