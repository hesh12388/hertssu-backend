package com.hertssu.profile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceStats {
    private Double avgTeamwork;
    private Double avgPerformance;
    private Double avgCommunication;
    private Double overallAverage;
    private Integer totalEvaluations;
    
    private String teamworkTrend; 
    private String performanceTrend;
    private String communicationTrend;
}