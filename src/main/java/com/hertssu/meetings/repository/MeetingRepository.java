package com.hertssu.meetings.repository;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hertssu.model.Meeting;
import com.hertssu.model.User;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Page<Meeting> findByDateAndCreatedBy(LocalDate date, User createdBy, Pageable pageable);

    Page<Meeting> findByDateAfterAndCreatedBy(LocalDate date, User createdBy, Pageable pageable);

    Page<Meeting> findByDateBetweenAndCreatedBy(LocalDate start, LocalDate end, User createdBy, Pageable pageable);

    Page<Meeting> findByTitleContainingIgnoreCaseAndCreatedBy(String title, User createdBy, Pageable pageable);


      @Query("""
        select m from Meeting m
        where m.createdBy.id = :userId
        and (m.date < :today or upper(m.meetingStatus) in :statuses)
        order by m.date desc, m.startTime desc
    """)
    Page<Meeting> findHistory(
        @Param("userId") Long userId,
        @Param("today") LocalDate today,
        @Param("statuses") Collection<String> statuses,
        Pageable pageable
    );
}