// com.hertssu.meetings.dto.UpdateMeetingRequest.java
package com.hertssu.meetings.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateMeetingRequest {
    private String title;
    private String description;
    private String location;

    // ISO strings from the client
    private String date;       // "YYYY-MM-DD"
    private String startTime;  // "HH:mm" or "HH:mm:ss"
    private String endTime;    // "HH:mm" or "HH:mm:ss"

    private Boolean isAllDay;

    // participants as emails, same as create
    private List<String> participants;

    private String recurrenceRule;     // optional
    private List<Integer> reminders;   // optional
}
