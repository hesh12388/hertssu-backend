package com.hertssu.meetings.controller;

import com.hertssu.meetings.dto.MeetingPerformanceUpdateRequest;
import com.hertssu.meetings.dto.MeetingEvaluation.MeetingEvaluationRequest;
import com.hertssu.meetings.dto.MeetingEvaluation.MeetingEvaluationResponse;
import com.hertssu.meetings.service.MeetingEvaluationService;
import com.hertssu.security.AuthUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/meetings/{meetingId}/evaluations")
@RequiredArgsConstructor
public class MeetingEvaluationController {

    private final MeetingEvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<?> createEvaluation(
            @PathVariable Long meetingId,
            @RequestBody MeetingEvaluationRequest req,
            @AuthenticationPrincipal AuthUserPrincipal me   
    ) {
        return ResponseEntity.ok(evaluationService.evaluateParticipant(meetingId, req, me));
    }

    @GetMapping
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<List<MeetingEvaluationResponse>> listEvaluations(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var list = evaluationService.getEvaluations(meetingId, currentUser);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{evaluationId}/performance")
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<MeetingEvaluationResponse> patchPerformance(
            @PathVariable Long meetingId,
            @PathVariable UUID evaluationId,
            @RequestBody MeetingPerformanceUpdateRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var updated = evaluationService.updatePerformance(meetingId, evaluationId, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{evaluationId}/performance")
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<MeetingEvaluationResponse> deletePerformance(
            @PathVariable Long meetingId,
            @PathVariable UUID evaluationId,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var updated = evaluationService.deletePerformance(meetingId, evaluationId, currentUser);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{evaluationId}")
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<MeetingEvaluationResponse> putEvaluation(
            @PathVariable Long meetingId,
            @PathVariable UUID evaluationId,
            @RequestBody MeetingEvaluationRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var updated = evaluationService.updateFull(meetingId, evaluationId, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{evaluationId}")
    @PreAuthorize("@meetingGuard.isCreator(#meetingId, authentication)")
    public ResponseEntity<Void> deleteEvaluation(
            @PathVariable Long meetingId,
            @PathVariable UUID evaluationId,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        evaluationService.deleteEvaluation(meetingId, evaluationId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
