package com.hertssu.interview.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewLogRequest {
    private Integer performance;
    private Integer experience;
    private Integer communication;
    private Integer teamwork;
    private Integer confidence;
    private Boolean accepted;
    private String notes;
}
