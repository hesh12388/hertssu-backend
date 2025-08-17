package com.hertssu.meetings.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateMeetingRequest {
    private String title;
    private String description;
    private String location;
    private String date;          // "2025-08-17"
    private String startTime;     // "14:00"
    private String endTime;       // "15:00"
    private Boolean isAllDay;
    private String recurrenceRule;
    private List<Integer> reminders;
    private List<String> participants; // just emails
    private String recurrenceUntil; 
}
