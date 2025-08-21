package com.hertssu.meetings;

import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.MeetingResponse;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.security.AuthUserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/meetings")
@Slf4j
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        MeetingResponse response = meetingService.createMeeting(request, me);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable Long meetingId,
            @Valid @RequestBody UpdateMeetingRequest request
            ) {
        
        log.info("Updating meeting: {} by user: {}", meetingId);
        
        MeetingResponse response = meetingService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long meetingId) {
        
        log.info("Deleting meeting: {} ", meetingId);
        
        meetingService.deleteMeeting(meetingId);
        
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<MeetingResponse>> getUpcomingMeetings(
            @PageableDefault(size = 20, sort = {"date", "startTime"}) Pageable pageable,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        log.debug("Getting upcoming meetings for user: {}", me.getName());
        
        Page<MeetingResponse> response = meetingService.getUpcomingMeetings(pageable, me);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<MeetingResponse>> getHistoryMeetings(
            @PageableDefault(size = 20, sort = {"date", "startTime"}) Pageable pageable,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        log.debug("Getting history meetings for user: {}", me.getName());
        
        Page<MeetingResponse> response = meetingService.getHistoryMeetings(pageable, me);
        return ResponseEntity.ok(response);
    }
}