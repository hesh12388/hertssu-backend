package com.hertssu.meetings.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hertssu.exceptions.MeetingNotFoundException;
import com.hertssu.exceptions.ZoomSyncException;
import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.*;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingService.class);

    private final MeetingRepository meetingRepository;
    private final ZoomMeetingService zoomMeetingService;
    private final UserRepository userRepository;
    private final MeetingEvaluationRepository evaluationRepository;
    private final Clock clock;

    @Transactional
    public Meeting createMeeting(CreateMeetingRequest req, AuthUserPrincipal creator) {
        User user = userRepository.findById(creator.getId())
        .orElseThrow(() -> new MeetingNotFoundException(creator.getId()));


        ZoneId zone = resolveZone(user);


        Meeting meeting = new Meeting();
        meeting.setTitle(req.getTitle());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());


        meeting.setDate(LocalDate.parse(req.getDate()));


        if (Boolean.TRUE.equals(req.getIsAllDay())) {
        meeting.setIsAllDay(true);
        meeting.setStartTime(LocalTime.MIDNIGHT);
        meeting.setEndTime(LocalTime.of(23, 59));
        } else {
        meeting.setIsAllDay(false);
        meeting.setStartTime(parseTime(req.getStartTime()));
        meeting.setEndTime(parseTime(req.getEndTime()));
        }


        if (req.getParticipants() != null) {
            List<String> emails = req.getParticipants();
            List<User> found = emails.isEmpty() ? List.of() : userRepository.findByEmailIn(emails);
            Set<String> foundEmails = found.stream().map(User::getEmail).collect(Collectors.toSet());
            List<String> missing = emails.stream().filter(e -> !foundEmails.contains(e)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown participant emails: " + missing);
        }
        meeting.setParticipants(found);

        } else {
            meeting.setParticipants(List.of());
        }


        meeting.setMeetingStatus("SCHEDULED");
        meeting.setCreatedAt(LocalDateTime.now(clock));
        meeting.setUpdatedAt(meeting.getCreatedAt());
        meeting.setCreatedBy(user);
        meeting.setUpdatedBy(user);

        meeting = meetingRepository.save(meeting);


        try {
        String startIso;
        String endIso;
        if (meeting.getIsAllDay()) {
        startIso = toIsoOffset(meeting.getDate(), LocalTime.MIDNIGHT, zone);
        // end exclusive next day midnight for Zoom
        endIso = toIsoOffset(meeting.getDate().plusDays(1), LocalTime.MIDNIGHT, zone);
        } else {
        startIso = toIsoOffset(meeting.getDate(), meeting.getStartTime(), zone);
        endIso = toIsoOffset(meeting.getDate(), meeting.getEndTime(), zone);
        }


        String[] participantEmails = meeting.getParticipants().stream().map(User::getEmail).toArray(String[]::new);


        var response = zoomMeetingService.createMeeting(
        meeting.getTitle(), startIso, endIso, participantEmails, user
        );


        if (response != null) {
        meeting.setZoomMeetingId(response.getZoomMeetingId());
        meeting.setJoinUrl(response.getZoomJoinUrl());
        }
        return meetingRepository.save(meeting);
        } catch (Exception e) {
        logger.error("Zoom create failed for meeting id={}: {}", meeting.getMeetingId(), e.getMessage(), e);
        throw new ZoomSyncException("Failed to create Zoom meeting", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getUpcomingMeetings(Long userId, Pageable pageable) {
        LocalDate nowDate = LocalDate.now(clock);
        LocalTime nowTime = LocalTime.now(clock);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MeetingNotFoundException(userId));

    return meetingRepository.findHistoryVisibleForUser(nowDate, nowTime, user, pageable);    
}



    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
            .orElseThrow(() -> new MeetingNotFoundException(id));
    }



    @Transactional
    public void deleteMeeting(Long id) {
        Meeting meeting = getMeetingById(id);
        try{
            if (meeting.getZoomMeetingId() != null) {
                zoomMeetingService.cancelMeeting(meeting.getZoomMeetingId(), meeting.getCreatedBy());
            }
        } catch (Exception e){
            throw new ZoomSyncException("Failed to cancel Zoom meeting", e);
        }
        evaluationRepository.deleteByMeeting_MeetingId(id);
        meetingRepository.deleteById(id);
        logger.info("Meeting id={} permanently deleted", id);
    }




    @Transactional
    public Meeting updateMeeting(Long id, UpdateMeetingRequest req, AuthUserPrincipal currentUser) {
        Meeting existing = getMeetingById(id);
        User updater = userRepository.getReferenceById(currentUser.getId());
    
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getLocation() != null) existing.setLocation(req.getLocation());
        if (req.getDate() != null) existing.setDate(LocalDate.parse(req.getDate()));

        if (Boolean.TRUE.equals(req.getIsAllDay())) {
            existing.setIsAllDay(true);
            existing.setStartTime(LocalTime.of(0, 0));
            existing.setEndTime(LocalTime.of(23, 59));
        } else if (req.getIsAllDay() != null && !req.getIsAllDay()) {
            existing.setIsAllDay(false);
            if (req.getStartTime() != null) existing.setStartTime(parseTime(req.getStartTime()));
            if (req.getEndTime() != null) existing.setEndTime(parseTime(req.getEndTime()));
        } else {
            if (req.getStartTime() != null) existing.setStartTime(parseTime(req.getStartTime()));
            if (req.getEndTime() != null) existing.setEndTime(parseTime(req.getEndTime()));
        }

        if (req.getParticipants() != null) {
            List<String> emails = req.getParticipants();
            List<User> found = emails.isEmpty() ? List.of() : userRepository.findByEmailIn(emails);
            Set<String> foundEmails = found.stream().map(User::getEmail).collect(Collectors.toSet());
            List<String> missing = emails.stream().filter(e -> !foundEmails.contains(e)).toList();
            if (!missing.isEmpty()){
                throw new IllegalArgumentException("Participants not found: " + missing);
            }
            existing.setParticipants(found);
        }

        existing.setUpdatedAt(LocalDateTime.now(clock));
        existing.setUpdatedBy(updater); 

        ZoneId zone = resolveZone(existing.getCreatedBy() != null ? existing.getCreatedBy() : updater);

        if (existing.getZoomMeetingId() != null) {
            try {
                String startIso;
                String endIso;

                if (Boolean.TRUE.equals(existing.getIsAllDay())){
                    startIso = toIsoOffset(existing.getDate(), LocalTime.MIDNIGHT, zone);
                    endIso = toIsoOffset(existing.getDate().plusDays(1), LocalTime.MIDNIGHT, zone);
                } else {
                    startIso = toIsoOffset(existing.getDate(), existing.getStartTime(), zone);
                    endIso = toIsoOffset(existing.getDate(), existing.getEndTime(), zone);
                }

                String[] participantEmails = existing.getParticipants() == null ? new String[0]
                        : existing.getParticipants().stream().map(User::getEmail).toArray(String[]::new);

                zoomMeetingService.updateMeeting(
                    existing.getZoomMeetingId(),
                    existing.getTitle(),
                    startIso,
                    endIso,
                    participantEmails,
                    existing.getCreatedBy()
                );
                logger.info(" Zoom meeting update success for {}", existing.getZoomMeetingId());
            } catch (Exception e) {
                logger.error(" Zoom update failed for {}: {}", existing.getZoomMeetingId(), e.getMessage(), e);
                throw new ZoomSyncException("Failed to update Zoom meeting", e);
            }
        }

        return meetingRepository.save(existing);
    }


    private static LocalTime parseTime(String t) {
        if (t == null) return null;
        if (t.length() == 5) return LocalTime.parse(t + ":00");
        return LocalTime.parse(t);
    }


    
    private static String toIsoOffset(LocalDate d, LocalTime t, ZoneId zone) {
        if (d == null || t == null) throw new IllegalArgumentException("Date and time required");
        return d.atTime(t)
            .atZone(zone)
            .toOffsetDateTime()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }





    private ZoneId resolveZone(User user) {
        return ZoneId.systemDefault();
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getHistoryMeetings(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        LocalTime nowTime = LocalTime.now(clock);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MeetingNotFoundException(userId));

        return meetingRepository.findHistoryVisibleForUser(today, nowTime, user, pageable);
    }

}