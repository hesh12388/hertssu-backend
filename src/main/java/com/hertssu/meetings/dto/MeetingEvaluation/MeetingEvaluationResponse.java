// MeetingEvaluationResponse.java
package com.hertssu.meetings.dto.MeetingEvaluation;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class MeetingEvaluationResponse {
    private UUID id;
    private Long meetingId;
    private Long participantId;
    private String participantName;
    private Long evaluatorId;
    private String evaluatorName;
    private Integer performance;
    private Integer communication;
    private Integer teamwork;
    private String notes;
    private Boolean attendance;
    private Boolean isLate;
}
