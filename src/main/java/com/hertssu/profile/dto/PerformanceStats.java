package com.hertssu.profile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceStats {
    private Double avgPerformance;
    private Integer totalEvaluations;
    private Integer totalAbsences;
    private Integer totalLateArrivals;
    private Integer totalExceptions;
    private Double attendanceRate;
}