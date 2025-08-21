package com.hertssu.meetings.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateMeetingRequest {
    private String title;
    private String description;
    private String location;
    private String date;          
    private String startTime;     
    private String endTime;      
    private Boolean isAllDay;
    private List<String> participants; 
}
