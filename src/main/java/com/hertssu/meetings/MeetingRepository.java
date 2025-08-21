package com.hertssu.meetings;

import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p = :user AND m.date >= :currentDate ORDER BY m.date ASC, m.startTime ASC")
    Page<Meeting> findUpcomingMeetingsForUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate, Pageable pageable);
    
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p = :user AND m.date < :currentDate ORDER BY m.date DESC, m.startTime DESC")
    Page<Meeting> findHistoryMeetingsForUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate, Pageable pageable);
}