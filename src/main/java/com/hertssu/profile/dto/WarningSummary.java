package com.hertssu.profile.dto;

import java.time.LocalDateTime;

import com.hertssu.Warnings.dto.WarningSeverity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarningSummary {
    private Long id;
    private String reason;
    private WarningSeverity severity;
    private LocalDateTime issuedDate;
    private String assignerName;
    private String actionTaken;
}