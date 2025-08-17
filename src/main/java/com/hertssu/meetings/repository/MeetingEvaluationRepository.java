package com.hertssu.meetings.repository;

import com.hertssu.model.MeetingEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface MeetingEvaluationRepository extends JpaRepository<MeetingEvaluation, UUID> {
    List<MeetingEvaluation> findByMeetingMeetingId(Long meetingId);
    List<MeetingEvaluation> findByMeetingMeetingIdAndMeetingCreatedById(Long meetingId, Long createdById);
    Optional<MeetingEvaluation> findByIdAndMeetingMeetingIdAndMeetingCreatedById(UUID id, Long meetingId, Long createdById);
    void deleteByMeeting_MeetingId(Long meetingId);
}
