package com.hertssu.profile;
import com.hertssu.profile.dto.*;
import com.hertssu.user.UserRepository;
import com.hertssu.model.*;
import com.hertssu.Tasks.TaskRepository;
import com.hertssu.Warnings.WarningRepository;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final TaskRepository taskRepository;
    private final WarningRepository warningRepository;
    private final MeetingEvaluationRepository meetingEvaluationRepository;
    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        // Get all data
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        List<Warning> warnings = warningRepository.findByAssigneeIdOrderByIssuedDateDesc(userId);
        List<MeetingEvaluation> evaluations = meetingEvaluationRepository.findByParticipantIdOrderByMeetingDateDesc(userId);

        // Convert to DTOs
        List<TaskSummary> taskSummaries = tasks.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());

        List<WarningSummary> warningSummaries = warnings.stream()
                .map(this::convertToWarningSummary)
                .collect(Collectors.toList());

        List<PerformanceEvaluationResponse> evaluationResponses = evaluations.stream()
                .map(this::convertToPerformanceEvaluationResponse)
                .collect(Collectors.toList());

        // Calculate performance stats
        PerformanceStats performanceStats = calculatePerformanceStats(evaluations);

        // Get user basic info
        UserBasicInfo userInfo = getUserBasicInfo(userId);

        return UserProfileResponse.builder()
                .user(userInfo)
                .tasks(taskSummaries)
                .performanceEvaluations(evaluationResponses)
                .warnings(warningSummaries)
                .performanceStats(performanceStats)
                .build();
    }

    private TaskSummary convertToTaskSummary(Task task) {
        return TaskSummary.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .submittedAt(task.getSubmittedAt())
                .assignerName(getFullName(task.getAssigner()))
                .build();
    }

    private WarningSummary convertToWarningSummary(Warning warning) {
        return WarningSummary.builder()
                .id(warning.getId())
                .reason(warning.getReason())
                .severity(warning.getSeverity())
                .issuedDate(warning.getIssuedDate())
                .assignerName(getFullName(warning.getAssigner()))
                .actionTaken(warning.getActionTaken())
                .build();
    }

    private PerformanceEvaluationResponse convertToPerformanceEvaluationResponse(MeetingEvaluation evaluation) {
        return PerformanceEvaluationResponse.builder()
                .meetingTitle(evaluation.getMeeting().getTitle())
                .meetingDate(evaluation.getMeeting().getDate())
                .performance(evaluation.getPerformance())
                .communication(evaluation.getCommunication())
                .teamwork(evaluation.getTeamwork())
                .evaluatorName(getFullName(evaluation.getEvaluator()))
                .createdAt(evaluation.getCreatedAt())
                .notes(evaluation.getNotes())
                .build();
    }

    private PerformanceStats calculatePerformanceStats(List<MeetingEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return PerformanceStats.builder()
                    .avgTeamwork(0.0)
                    .avgPerformance(0.0)
                    .avgCommunication(0.0)
                    .overallAverage(0.0)
                    .totalEvaluations(0)
                    .build();
        }

        double avgTeamwork = evaluations.stream()
                .mapToInt(MeetingEvaluation::getTeamwork)
                .average()
                .orElse(0.0);

        double avgPerformance = evaluations.stream()
                .mapToInt(MeetingEvaluation::getPerformance)
                .average()
                .orElse(0.0);

        double avgCommunication = evaluations.stream()
                .mapToInt(MeetingEvaluation::getCommunication)
                .average()
                .orElse(0.0);

        double overallAverage = (avgTeamwork + avgPerformance + avgCommunication) / 3.0;

        return PerformanceStats.builder()
                .avgTeamwork(Math.round(avgTeamwork * 100.0) / 100.0) // Round to 2 decimal places
                .avgPerformance(Math.round(avgPerformance * 100.0) / 100.0)
                .avgCommunication(Math.round(avgCommunication * 100.0) / 100.0)
                .overallAverage(Math.round(overallAverage * 100.0) / 100.0)
                .totalEvaluations(evaluations.size())
                .build();
    }

    private UserBasicInfo getUserBasicInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return UserBasicInfo.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private String getFullName(User user) {
        if (user == null) return "Unknown";
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}