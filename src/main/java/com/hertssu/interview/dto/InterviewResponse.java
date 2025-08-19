package com.hertssu.interview.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InterviewResponse {
    private UUID id;
    private String name;
    private String gafEmail;
    private String phoneNumber;
    private String gafId;
    private String position;
    private CommitteeSummary committee;
    private SubcommitteeSummary subCommittee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // Logging fields
    private Integer performance;
    private Integer experience;
    private Integer communication;
    private Integer teamwork;
    private Integer confidence;
    private Boolean accepted;
    private String notes;

    // Meta
    private String interviewerName;
    private String interviewerEmail;

    // Teams meeting fields
    private String meetingId;
    private String joinUrl;
    private String meetingPassword;
}