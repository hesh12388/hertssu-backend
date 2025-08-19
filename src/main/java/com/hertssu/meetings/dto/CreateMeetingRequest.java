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
    private String recurrenceRule;
    private List<Integer> reminders;
    private List<String> participants; 
    private String recurrenceUntil; 
}
