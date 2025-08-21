package com.hertssu.meetings.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {
    
    private Long evaluationId;
    private Long meetingId;
    private String meetingTitle;
    private UserResponse user;
    private UserResponse evaluatedBy;
    private Integer performance;
    private Boolean attended;
    private String note;
    private Boolean late;
    private Boolean hasException;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
