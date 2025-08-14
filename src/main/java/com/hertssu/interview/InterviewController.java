package com.hertssu.interview;

import com.hertssu.interview.dto.InterviewScheduleRequest;
import com.hertssu.interview.dto.InterviewUpdateRequest;
import com.hertssu.interview.dto.InterviewLogRequest;
import com.hertssu.interview.dto.InterviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import com.hertssu.security.AuthUserPrincipal;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMMITTEE_HR')")
    public ResponseEntity<InterviewResponse> scheduleInterview(@RequestBody InterviewScheduleRequest request, @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        InterviewResponse created = interviewService.scheduleInterview(request, currentUser);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}/log")
    @PreAuthorize("hasAuthority('COMMITTEE_HR')")
    public ResponseEntity<InterviewResponse> logInterview(
            @PathVariable UUID id,
            @RequestBody InterviewLogRequest request
    ) {
        
        InterviewResponse logged = interviewService.logInterview(id, request);
        return ResponseEntity.ok(logged);
      
        
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMMITTEE_HR') or hasAnyRole('PRESIDENT','VICE_PRESIDENT', 'EXECUTIVE_OFFICER','OFFICER')")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(@AuthenticationPrincipal AuthUserPrincipal currentUser) {
        List<InterviewResponse> responses = interviewService.getMyInterviews(currentUser);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMMITTEE_HR')")
    public ResponseEntity<Void> deleteInterview(@PathVariable UUID id, @AuthenticationPrincipal AuthUserPrincipal currentUser) {
        interviewService.deleteInterview(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMMITTEE_HR')")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable UUID id,
            @RequestBody InterviewUpdateRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        InterviewResponse updated = interviewService.updateInterview(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }
    }
