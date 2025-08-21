package com.hertssu.meetings;

import com.hertssu.meetings.dto.CreateEvaluationRequest;
import com.hertssu.meetings.dto.UpdateEvaluationRequest;
import com.hertssu.meetings.dto.EvaluationResponse;
import com.hertssu.security.AuthUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings/evaluations")
@Slf4j
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<EvaluationResponse> createEvaluation(
            @Valid @RequestBody CreateEvaluationRequest request,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        log.info("Creating evaluation for meeting: {} user: {}", request.getMeetingId(), request.getUserId());
        
        EvaluationResponse response = evaluationService.createEvaluation(request, me);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{evaluationId}")
    public ResponseEntity<EvaluationResponse> updateEvaluation(
            @PathVariable Long evaluationId,
            @Valid @RequestBody UpdateEvaluationRequest request,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        log.info("Updating evaluation: {}", evaluationId);
        
        EvaluationResponse response = evaluationService.updateEvaluation(evaluationId, request, me);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meeting/{meetingId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        log.debug("Getting evaluations for meeting: {}", meetingId);
        
        List<EvaluationResponse> response = evaluationService.getEvaluationsByMeeting(meetingId, me);
        return ResponseEntity.ok(response);
    }
}