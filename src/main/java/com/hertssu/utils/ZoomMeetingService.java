package com.hertssu.utils;

import com.hertssu.model.User;
import com.hertssu.utils.dto.MeetingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoomMeetingService {
    
    private final ZoomTokenProvider tokenProvider;
    
    @Value("${zoom.user-id:me}")
    private String zoomUserId;
    
    private final WebClient web = WebClient.builder()
            .baseUrl("https://api.zoom.us/v2")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
    
    // CREATE meeting
    public MeetingResponse createMeeting(String subject, String startIso, String endIso, 
                                       String[] attendeeEmails, User me) {
        String bearer = "Bearer " + tokenProvider.getAccessToken();
        System.out.println("Creating Zoom meeting for user: " + bearer);
        var body = buildMeetingPayload(subject, startIso, endIso, attendeeEmails);
        
        try {
            MeetingResponse response = web.post()
                    .uri("/users/{userId}/meetings", zoomUserId)
                    .header("Authorization", bearer)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(MeetingResponse.class)
                    .block();
            
            return response;
        } catch (Exception e) {
            log.error("Full error details: ", e);
            throw e;
        }
    }
    
    // UPDATE meeting
    public void updateMeeting(String meetingId, String subject, String startIso, String endIso, 
                            String[] attendeeEmails, User me) {
        String bearer = "Bearer " + tokenProvider.getAccessToken();
        var body = buildMeetingPayload(subject, startIso, endIso, attendeeEmails);
        
        try {
            web.patch()
                    .uri("/meetings/{meetingId}", meetingId)
                    .header("Authorization", bearer)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            
            log.info("Updated Zoom meeting: {}", meetingId);
            
        } catch (Exception e) {
            log.error("Failed to update Zoom meeting: {}", meetingId, e);
            throw new RuntimeException("Failed to update meeting", e);
        }
    }
    
    // CANCEL (DELETE) meeting
    public void cancelMeeting(String meetingId, User me) {
        String bearer = "Bearer " + tokenProvider.getAccessToken();
        
        try {
            web.delete()
                    .uri("/meetings/{meetingId}", meetingId)
                    .header("Authorization", bearer)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            
            log.info("Cancelled Zoom meeting: {}", meetingId);
            
        } catch (Exception e) {
            log.error("Failed to cancel Zoom meeting: {}", meetingId, e);
            throw new RuntimeException("Failed to cancel meeting", e);
        }
    }

    
    // Build Zoom meeting payload
    private Map<String, Object> buildMeetingPayload(String subject, String startIso, String endIso, String[] emails) {
        
        OffsetDateTime start = OffsetDateTime.parse(startIso);
        OffsetDateTime end = OffsetDateTime.parse(endIso);
        
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        
        String zoomStartTime = start.atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        
        // invitees list
        List<Map<String, String>> meetingInvitees = List.of(emails).stream()
                .map(email -> Map.of("email", email))
                .toList();
        
        // settings
        Map<String, Object> settings = Map.of(
            "host_video", true,
            "participant_video", true,
            "join_before_host", true,
            "mute_upon_entry", false,
            "watermark", false,
            "use_pmi", false,
            "approval_type", 0,
            "audio", "both", 
            "auto_recording", "none",
            "enforce_login", false
        );
        
       
        Map<String, Object> allSettings = new java.util.HashMap<>(settings);
        allSettings.put("waiting_room", false);
        allSettings.put("meeting_invitees", meetingInvitees);
                
        return Map.of(
            "topic", subject,
            "type", 2,
            "start_time", zoomStartTime,
            "duration", (int) durationMinutes,
            "timezone", "UTC",
            "agenda", "Meeting created via Student Union App",
            "default_password", true,
            "settings", allSettings
        );
    }
}