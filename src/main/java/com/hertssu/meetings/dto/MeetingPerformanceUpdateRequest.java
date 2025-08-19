package com.hertssu.meetings.dto;
import lombok.Data;

@Data
public class MeetingPerformanceUpdateRequest {
    private Integer performance;  
    private Integer communication; 
    private Integer teamwork;      
}
