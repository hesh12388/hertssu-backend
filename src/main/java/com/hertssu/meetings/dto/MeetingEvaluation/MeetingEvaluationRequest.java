// MeetingEvaluationRequest.java
package com.hertssu.meetings.dto.MeetingEvaluation;
import lombok.Data;

@Data
public class MeetingEvaluationRequest {
    private Long participantId;
    private Integer performance;
    private Integer communication;
    private Integer teamwork;
    private String notes;
    private Boolean attendance;
    private Boolean isLate;
}
