package com.hertssu.profile.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceEvaluationResponse {
    private String meetingTitle;
    private LocalDate meetingDate;
    private Integer performance;
    private Integer communication;
    private Integer teamwork;
    private String evaluatorName;
    private LocalDateTime createdAt;
    private String notes;
}
