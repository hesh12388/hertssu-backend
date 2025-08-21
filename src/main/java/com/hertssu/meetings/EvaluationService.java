package com.hertssu.meetings;

import com.hertssu.meetings.dto.CreateEvaluationRequest;
import com.hertssu.meetings.dto.UpdateEvaluationRequest;
import com.hertssu.meetings.dto.EvaluationResponse;
import com.hertssu.meetings.dto.UserResponse;
import com.hertssu.model.Meeting;
import com.hertssu.model.MeetingEvaluation;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public EvaluationResponse createEvaluation(CreateEvaluationRequest request, AuthUserPrincipal me) {
        // Get evaluator (current user)
        User evaluator = userRepository.getReferenceById(me.getId());
        
        // Get meeting
        Meeting meeting = meetingRepository.getReferenceById(request.getMeetingId());
        
        // Get participant being evaluated
        User participant = userRepository.getReferenceById(request.getUserId());
       
        // Create evaluation
        MeetingEvaluation evaluation = MeetingEvaluation.builder()
            .meeting(meeting)
            .evaluator(evaluator)
            .participant(participant)
            .performance(request.getPerformance())
            .attended(request.getAttended())
            .note(request.getNote())
            .late(request.getLate())
            .hasException(request.getHasException())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        MeetingEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        return convertToResponse(savedEvaluation);
    }

    public EvaluationResponse updateEvaluation(Long evaluationId, UpdateEvaluationRequest request, AuthUserPrincipal me) {
        MeetingEvaluation evaluation = evaluationRepository.getReferenceById(evaluationId);

        // Update fields if provided
        if (request.getPerformance() != null) {
            evaluation.setPerformance(request.getPerformance());
        }
        if (request.getAttended() != null) {
            evaluation.setAttended(request.getAttended());
        }
        if (request.getNote() != null) {
            evaluation.setNote(request.getNote());
        }
        if (request.getLate() != null) {
            evaluation.setLate(request.getLate());
        }
        if (request.getHasException() != null) {
            evaluation.setHasException(request.getHasException());
        }

        evaluation.setUpdatedAt(LocalDateTime.now());
        MeetingEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        return convertToResponse(savedEvaluation);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByMeeting(Long meetingId, AuthUserPrincipal me) {
        Meeting meeting = meetingRepository.getReferenceById(meetingId);

        List<MeetingEvaluation> evaluations = evaluationRepository.findByMeeting(meeting);
        return evaluations.stream()
            .map(this::convertToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByUser(Long userId) {
        User participant = userRepository.getReferenceById(userId);

        List<MeetingEvaluation> evaluations = evaluationRepository.findByParticipant(participant);
        return evaluations.stream()
            .map(this::convertToResponse)
            .toList();
    }

    private EvaluationResponse convertToResponse(MeetingEvaluation evaluation) {
        return EvaluationResponse.builder()
            .evaluationId(evaluation.getId())
            .meetingId(evaluation.getMeeting().getMeetingId())
            .meetingTitle(evaluation.getMeeting().getTitle())
            .user(convertUserToResponse(evaluation.getParticipant()))
            .evaluatedBy(convertUserToResponse(evaluation.getEvaluator()))
            .performance(evaluation.getPerformance())
            .attended(evaluation.getAttended())
            .note(evaluation.getNote())
            .late(evaluation.getLate())
            .hasException(evaluation.getHasException())
            .createdAt(evaluation.getCreatedAt())
            .updatedAt(evaluation.getUpdatedAt())
            .build();
    }

    private UserResponse convertUserToResponse(User user) {
        if (user == null) return null;
        
        return UserResponse.builder()
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}