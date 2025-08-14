// com.hertssu.progression.HistoryService
package com.hertssu.progression;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.Warning;
import com.hertssu.warnings.WarningRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryService {

  private final MeetingRepository meetingRepository;
//   private final TaskRepository taskRepository;
  private final WarningRepository warningRepository;

  // Normalize the “finished” states used across the app
  private static final List<String> MEETING_HISTORY_STATUSES = List.of("COMPLETED", "CANCELLED");
  private static final List<String> TASK_DONE_STATUSES = List.of("DONE", "COMPLETED", "RESOLVED", "CLOSED");

  /** Meetings in the past or explicitly completed/cancelled. */
  public Page<Meeting> completedMeetings(Long userId, Pageable pageable) {
    LocalDate today = LocalDate.now();
    return meetingRepository.findHistory(userId, today, MEETING_HISTORY_STATUSES, pageable);
  }

  /** Tasks that are completed (by status or timestamp). */
//   public Page<Task> completedTasks(Long userId, Pageable pageable) {
//     return taskRepository.historyForUser(userId, TASK_DONE_STATUSES, pageable);
//   }

  /** All logged warnings (already “finished” by definition). */
  public Page<Warning> loggedWarnings(Long userId, Pageable pageable) {
    return warningRepository.historyForUser(userId, pageable);
  }
}
