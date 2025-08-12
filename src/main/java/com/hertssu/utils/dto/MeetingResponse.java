package com.hertssu.utils.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingResponse {
    private String id;
    private String subject;


    private TimeBlock start;                  
    private TimeBlock end;                    
    private OnlineMeeting onlineMeeting;    
    private Organizer organizer;             
    private List<Attendee> attendees;        

    public OffsetDateTime getStartDateTime() {
        return start == null ? null
            : ZonedDateTime.of(start.getDateTime(), ZoneId.of(start.getTimeZone())).toOffsetDateTime();
    }
    public OffsetDateTime getEndDateTime() {
        return end == null ? null
            : ZonedDateTime.of(end.getDateTime(), ZoneId.of(end.getTimeZone())).toOffsetDateTime();
    }
    public String getJoinUrl() {
        return onlineMeeting == null ? null : onlineMeeting.getJoinUrl();
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeBlock {
        private java.time.LocalDateTime dateTime;
        private String timeZone;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OnlineMeeting {
        private String joinUrl;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Organizer {
        private EmailAddress emailAddress;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attendee {
        private EmailAddress emailAddress;
        private String type;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailAddress {
        private String address;     
        private String name;   
    }
}
