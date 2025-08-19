package com.hertssu.profile.dto;

import java.util.List;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserProfileResponse {
    private UserBasicInfo user;
    private List<TaskSummary> tasks;
    private List<PerformanceEvaluationResponse> performanceEvaluations;
    private List<WarningSummary> warnings;
    private PerformanceStats performanceStats;
}
