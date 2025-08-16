package com.hertssu.utils.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingResponse {
    
    // Zoom meeting ID 
    @JsonProperty("id")
    private String id;
    
    // Meeting topic/subject
    @JsonProperty("topic")
    private String topic;
    
    // Join URL
    @JsonProperty("join_url")
    private String joinUrl;
    
    // Alternative join URL for web browsers
    @JsonProperty("start_url")
    private String startUrl;
    
    // Meeting password
    @JsonProperty("password")
    private String password;
    
    // Meeting start time
    @JsonProperty("start_time")
    private String startTime;
    
    // Meeting duration in minutes
    @JsonProperty("duration")
    private Integer duration;
    
    // Meeting timezone
    @JsonProperty("timezone")
    private String timezone;
    
    // Meeting status
    @JsonProperty("status")
    private String status;
    
    // Meeting UUID (unique identifier)
    @JsonProperty("uuid")
    private String uuid;
    
    // Host ID
    @JsonProperty("host_id")
    private String hostId;
    
    // Created at timestamp
    @JsonProperty("created_at")
    private String createdAt;
    
    // Convenience methods for database storage
    public String getZoomMeetingId() {
        return this.id;
    }
    
    public String getZoomJoinUrl() {
        return this.joinUrl;
    }
    
}