package com.hertssu.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScheduleRequest {
    private String name;
    private String gafEmail;
    private String phoneNumber;
    private String gafId;
    private String position;
    private Integer committeeId;   
    private Integer subCommitteeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long supervisorId;
}
