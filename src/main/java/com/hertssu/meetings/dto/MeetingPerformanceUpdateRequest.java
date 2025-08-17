package com.hertssu.meetings.dto;
import lombok.Data;

@Data
public class MeetingPerformanceUpdateRequest {
    private Integer performance;   // 0..5, null to remove
    private Integer communication; // 0..5, null to remove
    private Integer teamwork;      // 0..5, null to remove
}
