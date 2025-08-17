package com.hertssu.meetings.service;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hertssu.exceptions.MeetingNotFoundException;
import com.hertssu.exceptions.ZoomSyncException;
import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;
import com.hertssu.meetings.repository.MeetingNoteRepository;

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
    private final MeetingNoteRepository noteRepository;
@Transactional
public Meeting createMeeting(CreateMeetingRequest req, User creator) {
    try {
        logger.info("📥 Creating meeting with payload: {}", req);

        Meeting meeting = new Meeting();
        meeting.setTitle(req.getTitle());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());
        logger.info("📝 Basic fields set: title={}, location={}", req.getTitle(), req.getLocation());

        meeting.setDate(LocalDate.parse(req.getDate()));
        logger.info("📅 Parsed date: {}", meeting.getDate());

        meeting.setRecurrenceRule(req.getRecurrenceRule());
        meeting.setReminders(req.getReminders());
        logger.info("🔁 Recurrence={}, reminders={}", req.getRecurrenceRule(), req.getReminders());

        if (Boolean.TRUE.equals(req.getIsAllDay())) {
            meeting.setIsAllDay(true);
            meeting.setStartTime(LocalTime.of(0, 0));
            meeting.setEndTime(LocalTime.of(23, 59));
            logger.info("⏰ All-day meeting from 00:00 to 23:59");
        } else {
            meeting.setIsAllDay(false);
            LocalTime start = parseTime(req.getStartTime());
            LocalTime end = parseTime(req.getEndTime());
            meeting.setStartTime(start);
            meeting.setEndTime(end);
            logger.info("⏰ Timed meeting start={} end={}", start, end);
        }

        if (req.getParticipants() != null && !req.getParticipants().isEmpty()) {
            logger.info("👥 Fetching participants: {}", req.getParticipants());
            List<User> people = userRepository.findByEmailIn(req.getParticipants());
            logger.info("👥 Found {} participants in DB", people.size());
            meeting.setParticipants(people);
        } else {
            logger.info("👥 No participants provided");
            meeting.setParticipants(List.of());
        }

        meeting.setCreatedBy(creator);
        meeting.setMeetingStatus("SCHEDULED");
        meeting.setCreatedAt(LocalDateTime.now());

        logger.info("💾 Saving meeting with Zoom sync...");
        Meeting saved = saveWithZoom(meeting, creator);
        logger.info("✅ Meeting created successfully with id={} and zoomId={}", saved.getMeetingId(), saved.getZoomMeetingId());

        return saved;

    } catch (Exception e) {
        logger.error("❌ Error creating meeting: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to create meeting", e);
    }
}


    /**
     * Optimized method with proper pagination and offset/limit parameters
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getMeetingsInRange(Long userId, String from, String to, int offset, int limit) {
        try {
            logger.info("🔎 getMeetingsInRange called: userId={}, from={}, to={}, offset={}, limit={}",
                    userId, from, to, offset, limit);

            LocalDate startDate = LocalDate.parse(from);
            LocalDate endDate = LocalDate.parse(to);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MeetingNotFoundException(userId));

            int pageNumber = offset / limit;
            Pageable pageable = PageRequest.of(
                    pageNumber,
                    limit,
                    Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "startTime"))
            );

            Page<Meeting> result = meetingRepository.findVisibleInRangeForUser(startDate, endDate, user, pageable);
            logger.info("✅ Fetched {} meetings (page {}, total: {})",
                    result.getNumberOfElements(), pageNumber, result.getTotalElements());

            return result;

        } catch (MeetingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error in getMeetingsInRange: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch meetings in range", e);
        }
    }

    /**
     * Overloaded method to maintain backward compatibility
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getMeetingsInRange(Long userId, String from, String to, int size, Pageable pageable) {
        return getMeetingsInRange(userId, from, to, 0, size);
    }
    
    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        try {
            logger.info("🔎 Fetching all meetings from repository...");
            List<Meeting> meetings = meetingRepository.findAll();
            logger.info("✅ Found {} meetings", meetings.size());
            return meetings;
        } catch (Exception e) {
            logger.error("❌ Error fetching all meetings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all meetings", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getTodayMeetingsForUser(Long userId, Pageable pageable) {
        try {
            LocalDate today = LocalDate.now();
            logger.info("🔎 getTodayMeetingsForUser called: userId={}, today={}", userId, today);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MeetingNotFoundException(userId));

            Page<Meeting> result = meetingRepository.findByDateAndCreatedByAndDeletedFalse(today, user, pageable);
            logger.info("✅ Fetched {} meetings for today", result.getTotalElements());
            return result;

        } catch (MeetingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error in getTodayMeetingsForUser: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch today's meetings", e);
        }
    }

    /**
     * Optimized upcoming meetings with pagination
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getUpcomingMeetings(Long userId, int offset, int limit) {
        try {
            LocalDate today = LocalDate.now();
            logger.info("🔎 getUpcomingMeetings called: userId={}, today={}, offset={}, limit={}",
                    userId, today, offset, limit);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MeetingNotFoundException(userId));

            int pageNumber = offset / limit;
            Pageable pageable = PageRequest.of(
                    pageNumber,
                    limit,
                    Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "startTime"))
            );

            Page<Meeting> result = meetingRepository.findUpcomingVisibleForUser(today, user, pageable);
            logger.info("✅ Fetched {} upcoming meetings (page {}, total: {})",
                    result.getNumberOfElements(), pageNumber, result.getTotalElements());

            return result;

        } catch (MeetingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error in getUpcomingMeetings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch upcoming meetings", e);
        }
    }


    /**
     * Backward compatibility method
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getUpcomingMeetings(Long userId, Pageable pageable) {
        return getUpcomingMeetings(userId, 0, pageable.getPageSize());
    }

    /**
     * Optimized history meetings with pagination
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getHistoryMeetings(Long userId, int offset, int limit) {
        try {
            LocalDate nowDate = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            logger.info("🔎 getHistoryMeetings called: userId={}, nowDate={}, nowTime={}, offset={}, limit={}",
                    userId, nowDate, nowTime, offset, limit);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MeetingNotFoundException(userId));

            int pageNumber = offset / limit;
            Pageable pageable = PageRequest.of(
                    pageNumber,
                    limit,
                    Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "startTime"))
            );

            Page<Meeting> result = meetingRepository.findHistoryVisibleForUser(nowDate, nowTime, user, pageable);
            logger.info("✅ Fetched {} history meetings (page {}, total: {})",
                    result.getNumberOfElements(), pageNumber, result.getTotalElements());

            return result;

        } catch (Exception e) {
            logger.error("❌ Error in getHistoryMeetings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch history meetings", e);
        }
    }


    /**
     * Backward compatibility method
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getHistoryMeetings(Long userId, Pageable pageable) {
        return getHistoryMeetings(userId, 0, pageable.getPageSize());
    }

    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
            .orElseThrow(() -> new MeetingNotFoundException(id));
    }

    @Transactional
    public void deleteMeeting(Long id) {
        logger.info("Soft-deleting meeting id={}", id);

        Meeting meeting = getMeetingById(id);

        try {
            if (meeting.getZoomMeetingId() != null) {
                zoomMeetingService.cancelMeeting(meeting.getZoomMeetingId(), meeting.getCreatedBy());
            }
        } catch (Exception e) {
            logger.error("❌ Zoom cancel failed for meeting {}: {}", id, e.getMessage(), e);
            throw new ZoomSyncException("Failed to cancel Zoom meeting", e);
        }

        evaluationRepository.deleteByMeeting_MeetingId(id);
        noteRepository.deleteByMeeting_MeetingId(id);

        meeting.setMeetingStatus("DELETED");
        meeting.setDeleted(true);
        meeting.setDeletedAt(LocalDateTime.now());

        meetingRepository.save(meeting);
        logger.info("Meeting id={} marked as deleted", id);
    }

    @Transactional
    public Meeting updateMeeting(Long id, UpdateMeetingRequest req, Long currentUserId) {
        logger.info("➡️ Starting updateMeeting for id={}, currentUserId={}", id, currentUserId);

        Meeting existing = getMeetingById(id);

        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getLocation() != null) existing.setLocation(req.getLocation());
        if (req.getRecurrenceRule() != null) existing.setRecurrenceRule(req.getRecurrenceRule());
        if (req.getReminders() != null) existing.setReminders(req.getReminders());

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
            List<User> people = req.getParticipants().isEmpty()
                    ? List.of()
                    : userRepository.findByEmailIn(req.getParticipants());
            existing.setParticipants(people);
        }

        existing.setUpdatedAt(LocalDateTime.now());

        if (existing.getZoomMeetingId() != null) {
            try {
                String startIso = toIsoOffset(existing.getDate(), existing.getStartTime());
                String endIso   = toIsoOffset(existing.getDate(), existing.getEndTime());

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
                logger.info("✅ Zoom meeting update success for {}", existing.getZoomMeetingId());
            } catch (Exception e) {
                logger.error("❌ Zoom update failed for {}: {}", existing.getZoomMeetingId(), e.getMessage(), e);
                throw new ZoomSyncException("Failed to update Zoom meeting", e);
            }
        }

        return meetingRepository.save(existing);
    }

    @Transactional
    public void deleteMeetingSeries(String recurrenceId) {
        if (recurrenceId == null) {
            throw new IllegalArgumentException("RecurrenceId cannot be null for deleting a series");
        }
        logger.info("Soft-deleting all meetings with recurrenceId={}", recurrenceId);

        List<Meeting> meetings = meetingRepository.findByRecurrenceId(recurrenceId);
        for (Meeting m : meetings) {
            try {
                if (m.getZoomMeetingId() != null) {
                    zoomMeetingService.cancelMeeting(m.getZoomMeetingId(), m.getCreatedBy());
                }
            } catch (Exception e) {
                logger.error("❌ Zoom cancel failed for series meeting {}: {}", m.getMeetingId(), e.getMessage(), e);
                throw new ZoomSyncException("Failed to cancel Zoom meeting in series", e);
            }

            evaluationRepository.deleteByMeeting_MeetingId(m.getMeetingId());
            noteRepository.deleteByMeeting_MeetingId(m.getMeetingId());

            m.setMeetingStatus("DELETED");
            m.setDeleted(true);
            m.setDeletedAt(LocalDateTime.now());
        }

        meetingRepository.saveAll(meetings);
        logger.info("Meetings with recurrenceId={} marked as deleted", recurrenceId);
    }

    @Transactional
    public void updateSeries(String recurrenceId, UpdateMeetingRequest req) {
        if (recurrenceId == null) {
            throw new IllegalArgumentException("RecurrenceId cannot be null for updating a series");
        }

        logger.info("🔄 Updating entire series for recurrenceId={}, payload={}", recurrenceId, req);

        List<Meeting> meetings = meetingRepository.findByRecurrenceId(recurrenceId);

        for (Meeting m : meetings) {
            if (req.getTitle() != null) m.setTitle(req.getTitle());
            if (req.getDescription() != null) m.setDescription(req.getDescription());
            if (req.getLocation() != null) m.setLocation(req.getLocation());
            if (req.getRecurrenceRule() != null) m.setRecurrenceRule(req.getRecurrenceRule());
            if (req.getReminders() != null) m.setReminders(req.getReminders());

            if (req.getDate() != null) m.setDate(LocalDate.parse(req.getDate()));

            if (Boolean.TRUE.equals(req.getIsAllDay())) {
                m.setIsAllDay(true);
                m.setStartTime(LocalTime.of(0, 0));
                m.setEndTime(LocalTime.of(23, 59));
            } else if (req.getIsAllDay() != null && !req.getIsAllDay()) {
                m.setIsAllDay(false);
                if (req.getStartTime() != null) m.setStartTime(parseTime(req.getStartTime()));
                if (req.getEndTime() != null) m.setEndTime(parseTime(req.getEndTime()));
            } else {
                if (req.getStartTime() != null) m.setStartTime(parseTime(req.getStartTime()));
                if (req.getEndTime() != null) m.setEndTime(parseTime(req.getEndTime()));
            }

            if (req.getParticipants() != null) {
                List<User> people = req.getParticipants().isEmpty()
                        ? List.of()
                        : userRepository.findByEmailIn(req.getParticipants());
                m.setParticipants(people);
            }

            m.setUpdatedAt(LocalDateTime.now());
        }

        meetingRepository.saveAll(meetings);
        logger.info("✅ Updated {} meetings in series {}", meetings.size(), recurrenceId);
    }


    public Meeting saveWithZoom(Meeting meeting, User creator) {
        try {
            String startIso = toIsoOffset(meeting.getDate(), meeting.getStartTime());
            String endIso   = toIsoOffset(meeting.getDate(), meeting.getEndTime());

            String[] participantEmails = meeting.getParticipants()
                    .stream().map(User::getEmail).toArray(String[]::new);

            var response = zoomMeetingService.createMeeting(
                    meeting.getTitle(), startIso, endIso, participantEmails, creator
            );

            if (response != null) {
                meeting.setZoomMeetingId(response.getZoomMeetingId());
                meeting.setJoinUrl(response.getZoomJoinUrl());
            }
            return meetingRepository.save(meeting);

        } catch (Exception e) {
            logger.error("❌ Failed to create meeting with Zoom: {}", e.getMessage(), e);
            throw new ZoomSyncException("Failed to create meeting with Zoom", e);
        }
    }

    private static LocalTime parseTime(String t) {
        if (t == null) return null;
        if (t.length() == 5) return LocalTime.parse(t + ":00");
        return LocalTime.parse(t);
    }

    private static String toIsoOffset(LocalDate d, LocalTime t) {
        ZoneId zone = ZoneId.systemDefault();
        return d.atTime(t)
                .atZone(zone)
                .toOffsetDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getExpandedMeetingsInRange(Long userId, String from, String to, int offset, int limit) {
        try {
            logger.info("🚀 getExpandedMeetingsInRange called: userId={}, from={}, to={}, offset={}, limit={}",
                    userId, from, to, offset, limit);

            LocalDate startDate = LocalDate.parse(from);
            LocalDate endDate = LocalDate.parse(to);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new MeetingNotFoundException(userId));

            // Look back 1 month to catch recurring anchors
            LocalDate expandedStart = startDate.minusMonths(1);

            // 🔄 Use creator OR participant visibility
            List<Meeting> baseMeetings = meetingRepository.findVisibleInRangeForUserList(
                    expandedStart, endDate, user
            );

            logger.info("📊 Found {} base meetings to process", baseMeetings.size());

            // (keep your expansion logic as-is)
            List<Meeting> expandedOccurrences = new ArrayList<>();
            for (Meeting baseMeeting : baseMeetings) {
                if (baseMeeting.getRecurrenceRule() == null || baseMeeting.getRecurrenceRule().isBlank()) {
                    if (!baseMeeting.getDate().isBefore(startDate) && !baseMeeting.getDate().isAfter(endDate)) {
                        expandedOccurrences.add(baseMeeting);
                    }
                } else {
                    expandedOccurrences.addAll(generateRecurrenceOccurrences(baseMeeting, startDate, endDate));
                }
            }

            expandedOccurrences.sort((a, b) -> {
                int dateComparison = a.getDate().compareTo(b.getDate());
                return (dateComparison != 0) ? dateComparison : a.getStartTime().compareTo(b.getStartTime());
            });

            int total = expandedOccurrences.size();
            int fromIndex = Math.min(offset, total);
            int toIndex = Math.min(offset + limit, total);
            List<Meeting> pageContent = expandedOccurrences.subList(fromIndex, toIndex);

            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<Meeting> result = new PageImpl<>(pageContent, pageable, total);

            logger.info("✅ Returning {} expanded meetings (page {}, total: {})",
                    result.getNumberOfElements(), offset / limit, total);

            return result;

        } catch (MeetingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error in getExpandedMeetingsInRange: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch expanded meetings in range", e);
        }
    }


    /**
     * Generate individual meeting occurrences for a recurring meeting within the date range
     */
    private List<Meeting> generateRecurrenceOccurrences(Meeting baseMeeting, LocalDate rangeStart, LocalDate rangeEnd) {
        List<Meeting> occurrences = new ArrayList<>();
        
        String recurrenceRule = baseMeeting.getRecurrenceRule().toUpperCase().trim();
        LocalDate currentDate = baseMeeting.getDate();
        LocalDate until = baseMeeting.getRecurrenceUntil(); // Note: there's a typo in your original field name
        
        // Determine the recurrence pattern
        ChronoUnit incrementUnit;
        int incrementAmount = 1;
        
        switch (recurrenceRule) {
            case "DAILY":
                incrementUnit = ChronoUnit.DAYS;
                break;
            case "WEEKLY":
                incrementUnit = ChronoUnit.WEEKS;
                break;
            case "MONTHLY":
                incrementUnit = ChronoUnit.MONTHS;
                break;
            default:
                logger.warn("⚠️ Unsupported recurrence rule: {}. Treating as non-recurring.", recurrenceRule);
                return List.of(baseMeeting);
        }
        
        // Generate occurrences
        int maxOccurrences = 100; // Safety limit to prevent infinite loops
        int count = 0;
        
        while (count < maxOccurrences) {
            // Check if we've exceeded the recurrence end date
            if (until != null && currentDate.isAfter(until)) {
                break;
            }
            
            // Check if we've gone beyond our query range
            if (currentDate.isAfter(rangeEnd)) {
                break;
            }
            
            // Include this occurrence if it falls within our target range
            if (!currentDate.isBefore(rangeStart) && !currentDate.isAfter(rangeEnd)) {
                Meeting occurrence = createOccurrenceFromBase(baseMeeting, currentDate);
                occurrences.add(occurrence);
            }
            
            // Move to next occurrence
            currentDate = currentDate.plus(incrementAmount, incrementUnit);
            count++;
        }
        
        logger.debug("Generated {} occurrences for recurring meeting {} ({})", 
                    occurrences.size(), baseMeeting.getMeetingId(), recurrenceRule);
        
        return occurrences;
    }

    /**
     * Create a meeting occurrence from a base recurring meeting
     */
    private Meeting createOccurrenceFromBase(Meeting baseMeeting, LocalDate occurrenceDate) {
        // Create a copy of the base meeting with the new date
        Meeting occurrence = new Meeting();
        occurrence.setMeetingId(baseMeeting.getMeetingId()); // Keep same ID for the occurrence
        occurrence.setTitle(baseMeeting.getTitle());
        occurrence.setDescription(baseMeeting.getDescription());
        occurrence.setLocation(baseMeeting.getLocation());
        occurrence.setDate(occurrenceDate); // This is the key difference
        occurrence.setStartTime(baseMeeting.getStartTime());
        occurrence.setEndTime(baseMeeting.getEndTime());
        occurrence.setIsAllDay(baseMeeting.getIsAllDay());
        occurrence.setParticipants(baseMeeting.getParticipants());
        occurrence.setCreatedBy(baseMeeting.getCreatedBy());
        occurrence.setMeetingStatus(baseMeeting.getMeetingStatus());
        occurrence.setCreatedAt(baseMeeting.getCreatedAt());
        occurrence.setUpdatedAt(baseMeeting.getUpdatedAt());
        occurrence.setRecurrenceId(baseMeeting.getRecurrenceId());
        occurrence.setRecurrenceRule(baseMeeting.getRecurrenceRule());
        occurrence.setRecurrenceUntil(baseMeeting.getRecurrenceUntil());
        occurrence.setReminders(baseMeeting.getReminders());
        occurrence.setZoomMeetingId(baseMeeting.getZoomMeetingId());
        occurrence.setJoinUrl(baseMeeting.getJoinUrl());
        occurrence.setDeleted(baseMeeting.isDeleted());
        occurrence.setDeletedAt(baseMeeting.getDeletedAt());
        
        return occurrence;
    }
}