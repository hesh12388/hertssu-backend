package com.hertssu.interview.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InterviewUpdateRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String gafId;
    private String position;
    private String committee;
    private String subCommittee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

