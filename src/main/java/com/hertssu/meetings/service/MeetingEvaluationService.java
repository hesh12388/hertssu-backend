package com.hertssu.meetings.service;

import com.hertssu.meetings.dto.MeetingEvaluation.MeetingEvaluationRequest;
import com.hertssu.meetings.dto.MeetingEvaluation.MeetingEvaluationResponse;
import com.hertssu.meetings.dto.MeetingPerformanceUpdateRequest;
import com.hertssu.model.Meeting;
import com.hertssu.model.MeetingEvaluation;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingEvaluationService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingEvaluationRepository evaluationRepository;

    @Transactional
    public MeetingEvaluationResponse evaluateParticipant(Long meetingId, MeetingEvaluationRequest req, AuthUserPrincipal me) {
        log.info("➡️ evaluateParticipant called: meetingId={}, req={}, userId={}", meetingId, req, me.getId());

        // enforce creator-only again here for defense in depth
        Meeting meeting = meetingRepository.findByMeetingIdAndCreatedById(meetingId, me.getId())
                .orElseThrow(() -> {
                    log.error("❌ Access denied: user {} is not creator of meeting {}", me.getId(), meetingId);
                    return new AccessDeniedException("Only the meeting creator can submit evaluations.");
                });

        log.info("✅ Meeting {} found, created by user {}", meetingId, me.getId());

        // verify participant is part of this meeting
        boolean participantInMeeting = meeting.getParticipants() != null &&
                meeting.getParticipants().stream().anyMatch(u -> u.getId().equals(req.getParticipantId()));
        if (!participantInMeeting) {
            log.error("❌ Participant {} not found in meeting {}", req.getParticipantId(), meetingId);
            throw new IllegalArgumentException("Participant is not part of this meeting.");
        }

        log.info("✅ Participant {} is part of meeting {}", req.getParticipantId(), meetingId);

        User evaluator = userRepository.getReferenceById(me.getId());
        User participant = userRepository.getReferenceById(req.getParticipantId());

        MeetingEvaluation eval = MeetingEvaluation.builder()
                .meeting(meeting)
                .evaluator(evaluator)
                .participant(participant)
                .performance(safeCap(req.getPerformance()))
                .communication(safeCap(req.getCommunication()))
                .teamwork(safeCap(req.getTeamwork()))
                .build();

        evaluationRepository.save(eval);

        log.info("✅ Evaluation saved: id={}, meetingId={}, participantId={}, evaluatorId={}",
                eval.getId(), meetingId, req.getParticipantId(), me.getId());

        return toResponse(eval);
    }


    @Transactional(readOnly = true)
    public List<MeetingEvaluationResponse> getEvaluations(Long meetingId, AuthUserPrincipal me) {
        var list = evaluationRepository.findByMeetingMeetingIdAndMeetingCreatedById(meetingId, me.getId());
        if (list.isEmpty() && meetingRepository.findById(meetingId).isEmpty()) {
            throw new EntityNotFoundException("Meeting not found");
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public MeetingEvaluationResponse updatePerformance(Long meetingId, UUID evaluationId, MeetingPerformanceUpdateRequest req, AuthUserPrincipal me) {
        var eval = evaluationRepository.findByIdAndMeetingMeetingIdAndMeetingCreatedById(evaluationId, meetingId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("Not allowed or evaluation not found"));

        if (req.getPerformance() != null)   eval.setPerformance(safeCap(req.getPerformance()));
        if (req.getCommunication() != null) eval.setCommunication(safeCap(req.getCommunication()));
        if (req.getTeamwork() != null)      eval.setTeamwork(safeCap(req.getTeamwork()));

        evaluationRepository.save(eval);
        return toResponse(eval);
    }

    @Transactional
    public MeetingEvaluationResponse deletePerformance(Long meetingId, UUID evaluationId, AuthUserPrincipal me) {
        var eval = evaluationRepository.findByIdAndMeetingMeetingIdAndMeetingCreatedById(evaluationId, meetingId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("Not allowed or evaluation not found"));

        eval.setPerformance(null);
        eval.setCommunication(null);
        eval.setTeamwork(null);

        evaluationRepository.save(eval);
        return toResponse(eval);
    }

    @Transactional
    public MeetingEvaluationResponse updateFull(Long meetingId, UUID evaluationId, MeetingEvaluationRequest req, AuthUserPrincipal me) {
        var eval = evaluationRepository.findByIdAndMeetingMeetingIdAndMeetingCreatedById(evaluationId, meetingId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("Not allowed or evaluation not found"));

        if (req.getParticipantId() != null) {
            boolean participantInMeeting = eval.getMeeting().getParticipants() != null &&
                    eval.getMeeting().getParticipants().stream().anyMatch(u -> u.getId().equals(req.getParticipantId()));
            if (!participantInMeeting) throw new IllegalArgumentException("Participant not in this meeting.");
            var participant = userRepository.getReferenceById(req.getParticipantId());
            eval.setParticipant(participant);
        }

        if (req.getPerformance() != null)   eval.setPerformance(safeCap(req.getPerformance()));
        if (req.getCommunication() != null) eval.setCommunication(safeCap(req.getCommunication()));
        if (req.getTeamwork() != null)      eval.setTeamwork(safeCap(req.getTeamwork()));

        evaluationRepository.save(eval);
        return toResponse(eval);
    }

    @Transactional
    public void deleteEvaluation(Long meetingId, UUID evaluationId, AuthUserPrincipal me) {
        var eval = evaluationRepository.findByIdAndMeetingMeetingIdAndMeetingCreatedById(evaluationId, meetingId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("Not allowed or evaluation not found"));
        evaluationRepository.delete(eval);
    }

    private Integer safeCap(Integer v) {
        if (v == null) return 0;
        return Math.max(0, Math.min(5, v));
    }

    private MeetingEvaluationResponse toResponse(MeetingEvaluation eval) {
        return MeetingEvaluationResponse.builder()
                .id(eval.getId())
                .meetingId(eval.getMeeting().getMeetingId())
                .participantId(eval.getParticipant().getId())
                .participantName(eval.getParticipant().getFirstName())
                .evaluatorId(eval.getEvaluator().getId())
                .evaluatorName(eval.getEvaluator().getFirstName())
                .performance(eval.getPerformance())
                .communication(eval.getCommunication())
                .teamwork(eval.getTeamwork())
                .build();
    }
}
