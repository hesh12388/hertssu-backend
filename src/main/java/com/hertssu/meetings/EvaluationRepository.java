package com.hertssu.meetings;
import com.hertssu.model.Meeting;
import com.hertssu.model.MeetingEvaluation;
import com.hertssu.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<MeetingEvaluation, Long> {
    
    List<MeetingEvaluation> findByMeeting(Meeting meeting);
    
    List<MeetingEvaluation> findByParticipant(User user);
    List<MeetingEvaluation> findTop20ByParticipantOrderByMeetingDateDesc(User user);
}