package com.hertssu.meetings.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEvaluationRequest {
    
    @Min(value = 1, message = "Performance must be between 1 and 10")
    @Max(value = 10, message = "Performance must be between 1 and 10")
    private Integer performance;
    
    private Boolean attended;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
    
    private Boolean late;
    
    private Boolean hasException;
}