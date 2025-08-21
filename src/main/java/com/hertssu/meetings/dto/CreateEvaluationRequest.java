package com.hertssu.meetings.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvaluationRequest {
    
    @NotNull(message = "Meeting ID is required")
    private Long meetingId;
    
    @NotNull(message = "User ID is required") 
    private Long userId;
    
    @NotNull(message = "Performance rating is required")
    @Min(value = 1, message = "Performance must be between 1 and 10")
    @Max(value = 10, message = "Performance must be between 1 and 10")
    private Integer performance;
    
    @NotNull(message = "Attendance status is required")
    private Boolean attended;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
    
    @Builder.Default
    private Boolean late = false;
    
    @Builder.Default
    private Boolean hasException = false;
}