package com.hertssu.meetings.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hertssu.model.Meeting;
import com.hertssu.model.User;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Page<Meeting> findByTitleContainingIgnoreCaseAndCreatedBy(String title, User createdBy, Pageable pageable);

    void deleteByRecurrenceId(String recurrenceId);

    @EntityGraph(value = "Meeting.withParticipants", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Meeting> findByMeetingIdAndCreatedById(Long meetingId, Long createdById);

    @Query("SELECT m FROM Meeting m WHERE m.date BETWEEN :startDate AND :endDate " +
           "AND m.createdBy = :user AND m.deleted = false " +
           "ORDER BY m.date ASC, m.startTime ASC")
    List<Meeting> findByDateBetweenAndCreatedByAndDeletedFalseOrderByDateAscStartTimeAsc(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("user") User user
    );

    /**
     * Existing method with pagination (keep this for backward compatibility)
     */
    Page<Meeting> findByDateBetweenAndCreatedByAndDeletedFalse(
        LocalDate startDate, 
        LocalDate endDate, 
        User user, 
        Pageable pageable
    );

    /**
     * Find meetings for a specific date
     */
    Page<Meeting> findByDateAndCreatedByAndDeletedFalse(
        LocalDate date, 
        User user, 
        Pageable pageable
    );

    /**
     * Find upcoming meetings (after today)
     */
    Page<Meeting> findByDateAfterAndCreatedByAndDeletedFalse(
        LocalDate date, 
        User user, 
        Pageable pageable
    );

    /**
     * Find meetings by recurrence ID
     */
    List<Meeting> findByRecurrenceId(String recurrenceId);

    /**
     * 🔧 OPTIMIZED: Custom query for history meetings
     * This query finds meetings that have already started or ended
     */
    @Query("SELECT m FROM Meeting m WHERE m.createdBy.id = :userId " +
           "AND m.deleted = false " +
           "AND (m.date < :nowDate OR (m.date = :nowDate AND m.startTime <= :nowTime)) " +
           "ORDER BY m.date DESC, m.startTime DESC")
    Page<Meeting> findHistoryByUser(
        @Param("userId") Long userId,
        @Param("nowDate") LocalDate nowDate,
        @Param("nowTime") LocalTime nowTime,
        Pageable pageable
    );

    /**
     * 🚀 PERFORMANCE: Additional indexes for better query performance
     * Add these as database indexes:
     * 
     * CREATE INDEX idx_meeting_date_creator ON meetings(date, created_by_id);
     * CREATE INDEX idx_meeting_date_range ON meetings(date) WHERE deleted = false;
     * CREATE INDEX idx_meeting_recurrence ON meetings(recurrence_id) WHERE recurrence_id IS NOT NULL;
     * CREATE INDEX idx_meeting_user_date_time ON meetings(created_by_id, date, start_time) WHERE deleted = false;
     */

    /**
     * 🔧 OPTIMIZED: Count total meetings in range (useful for pagination metadata)
     */
    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.date BETWEEN :startDate AND :endDate " +
           "AND m.createdBy = :user AND m.deleted = false")
    long countByDateBetweenAndCreatedByAndDeletedFalse(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("user") User user
    );

    /**
     * 🔧 OPTIMIZED: Find recurring meetings that might generate occurrences in a date range
     * This helps with server-side recurrence expansion by finding base recurring meetings
     */
    @Query("SELECT m FROM Meeting m WHERE m.createdBy = :user AND m.deleted = false " +
           "AND m.recurrenceRule IS NOT NULL AND m.recurrenceRule != '' " +
           "AND (m.reccurenceUntil IS NULL OR m.reccurenceUntil >= :rangeStart) " +
           "AND m.date <= :rangeEnd " +
           "ORDER BY m.date ASC, m.startTime ASC")
    List<Meeting> findRecurringMeetingsForRange(
        @Param("user") User user,
        @Param("rangeStart") LocalDate rangeStart,
        @Param("rangeEnd") LocalDate rangeEnd
    );
@Query("""
    select distinct m
    from Meeting m
    left join m.participants p
    where m.deleted = false
      and m.date between :from and :to
      and (m.createdBy = :user or p = :user)
    order by m.date asc, m.startTime asc
  """)
  Page<Meeting> findVisibleInRangeForUser(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("user") User user,
      Pageable pageable
  );

  // List version (used by the expanded recurrence method)
  @Query("""
    select distinct m
    from Meeting m
    left join m.participants p
    where m.deleted = false
      and m.date between :from and :to
      and (m.createdBy = :user or p = :user)
    order by m.date asc, m.startTime asc
  """)
  List<Meeting> findVisibleInRangeForUserList(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("user") User user
  );

  @Query("""
    select distinct m
    from Meeting m
    left join m.participants p
    where m.deleted = false
      and m.date > :today
      and (m.createdBy = :user or p = :user)
    order by m.date asc, m.startTime asc
  """)
  Page<Meeting> findUpcomingVisibleForUser(
      @Param("today") LocalDate today,
      @Param("user") User user,
      Pageable pageable
  );

  @Query("""
    select distinct m
    from Meeting m
    left join m.participants p
    where m.deleted = false
      and (
        m.date < :today
        or (m.date = :today and m.endTime < :nowTime)
      )
      and (m.createdBy = :user or p = :user)
    order by m.date desc, m.startTime desc
  """)
  Page<Meeting> findHistoryVisibleForUser(
      @Param("today") LocalDate today,
      @Param("nowTime") java.time.LocalTime nowTime,
      @Param("user") User user,
      Pageable pageable
  );
    
}