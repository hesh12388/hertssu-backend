package com.hertssu.profile;
import com.hertssu.profile.dto.*;
import com.hertssu.user.UserRepository;
import com.hertssu.model.*;
import com.hertssu.Tasks.TaskRepository;
import com.hertssu.Warnings.WarningRepository;
import com.hertssu.meetings.EvaluationRepository;
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
    private final EvaluationRepository meetingEvaluationRepository;
    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        // Get last 10 tasks assigned to the user
        List<Task> tasks = taskRepository.findTop10ByAssigneeIdOrderByCreatedAtDesc(userId);
        
        // Get last 10 warnings for the user
        List<Warning> warnings = warningRepository.findTop10ByAssigneeIdOrderByIssuedDateDesc(userId);

        // Get the user
        User user = userRepository.getReferenceById(userId);
        
        // Get the last 20 performance evaluations for the user
        List<MeetingEvaluation> evaluations = meetingEvaluationRepository.findTop20ByParticipantOrderByMeetingDateDesc(user);
        
        List<TaskSummary> taskSummaries = tasks.stream()
            .map(this::convertToTaskSummary)
            .collect(Collectors.toList());
            
        List<WarningSummary> warningSummaries = warnings.stream()
            .map(this::convertToWarningSummary)
            .collect(Collectors.toList());
            
        List<PerformanceEvaluationResponse> evaluationResponses = evaluations.stream()
            .map(this::convertToPerformanceEvaluationResponse)
            .collect(Collectors.toList());
            
        PerformanceStats performanceStats = calculatePerformanceStats(evaluations);
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
                .evaluatorName(getFullName(evaluation.getEvaluator()))
                .createdAt(evaluation.getCreatedAt())
                .note(evaluation.getNote())
                .isLate(evaluation.getLate())
                .attendance(evaluation.getAttended())
                .hasException(evaluation.getHasException())
                .build();
    }

    private PerformanceStats calculatePerformanceStats(List<MeetingEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return PerformanceStats.builder()
                    .avgPerformance(0.0)
                    .totalEvaluations(0)
                    .totalAbsences(0)
                    .totalLateArrivals(0)
                    .totalExceptions(0)
                    .attendanceRate(0.0)
                    .build();
        }

        double avgPerformance = evaluations.stream()
                .filter(e -> e.getPerformance() != null)
                .mapToInt(MeetingEvaluation::getPerformance)
                .average()
                .orElse(0.0);

        int totalAbsences = (int) evaluations.stream()
                .filter(e -> Boolean.FALSE.equals(e.getAttended()))
                .count();

        int totalLateArrivals = (int) evaluations.stream()
                .filter(e -> Boolean.TRUE.equals(e.getLate()))
                .count();

        int totalExceptions = (int) evaluations.stream()
                .filter(e -> Boolean.TRUE.equals(e.getHasException()))
                .count();

        double attendanceRate = evaluations.isEmpty() ? 0.0 : 
                (double) (evaluations.size() - totalAbsences) / evaluations.size() * 100.0;

        return PerformanceStats.builder()
                .avgPerformance(Math.round(avgPerformance * 100.0) / 100.0)
                .totalEvaluations(evaluations.size())
                .totalAbsences(totalAbsences)
                .totalLateArrivals(totalLateArrivals)
                .totalExceptions(totalExceptions)
                .attendanceRate(Math.round(attendanceRate * 100.0) / 100.0)
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