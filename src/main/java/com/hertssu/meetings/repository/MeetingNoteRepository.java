package com.hertssu.meetings.repository;

import com.hertssu.model.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {

    List<MeetingNote> findByMeeting_MeetingIdOrderByCreatedAtDesc(Long meetingId);

    void deleteByMeeting_MeetingId(Long meetingId);
}
