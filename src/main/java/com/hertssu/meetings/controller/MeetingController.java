package com.hertssu.meetings.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.MeetingResponseDto;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.service.MeetingService;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MeetingController.class);
    private final MeetingService meetingService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<List<MeetingResponseDto>> createMeeting(
            @RequestBody CreateMeetingRequest req,
            @AuthenticationPrincipal AuthUserPrincipal user) {

        User creator = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate date = LocalDate.parse(req.getDate());
        LocalTime start = LocalTime.parse(req.getStartTime());
        LocalTime end   = LocalTime.parse(req.getEndTime());
        LocalDate until = (req.getRecurrenceUntil() != null && !req.getRecurrenceUntil().isBlank())
                ? LocalDate.parse(req.getRecurrenceUntil())
                : null;

        String recurrenceRule = (req.getRecurrenceRule() != null && !req.getRecurrenceRule().isBlank())
                ? req.getRecurrenceRule()
                : null;

        List<User> participants = userRepository.findByEmailIn(req.getParticipants());
        String recurrenceId = (recurrenceRule != null)
                ? UUID.randomUUID().toString()
                : null;

        List<MeetingResponseDto> createdDtos = new ArrayList<>();
        LocalDate currentDate = date;

        while (true) {
            if (until != null && currentDate.isAfter(until)) break;

            Meeting recurring = Meeting.builder()
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .location(req.getLocation())
                    .date(currentDate)
                    .startTime(start)
                    .endTime(end)
                    .isAllDay(req.isAllDay())
                    .participants(participants)
                    .createdBy(creator)
                    .meetingStatus("SCHEDULED")
                    .createdAt(LocalDateTime.now())
                    .recurrenceId(recurrenceId)
                    .recurrenceRule(recurrenceRule)
                    .reccurenceUntil(until)
                    .reminders(req.getReminders())
                    .build();

            Meeting saved = meetingService.saveWithZoom(recurring, creator);

            try {
                createdDtos.add(MeetingResponseDto.fromEntity(saved));
            } catch (Exception e) {
                e.printStackTrace(); // log root cause
                throw e;
            }

            if (recurrenceRule == null) break;

            switch (recurrenceRule.toUpperCase()) {
                case "DAILY": currentDate = currentDate.plusDays(1); break;
                case "WEEKLY": currentDate = currentDate.plusWeeks(1); break;
                case "MONTHLY": currentDate = currentDate.plusMonths(1); break;
                default: throw new IllegalArgumentException("Unsupported recurrence rule: " + recurrenceRule);
            }
            if (until == null) break;
        }

        return ResponseEntity.ok(createdDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(
                MeetingResponseDto.fromEntity(meetingService.getMeetingById(id))
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSingle(@PathVariable Long id) {
        logger.info("Deleting single meeting with id={}", id);
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}/all")
    public ResponseEntity<Void> deleteAll(@PathVariable String id) {
        logger.info("Deleting whole meeting series with recurrenceId={}", id);
        meetingService.deleteMeetingSeries(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MeetingResponseDto> update(
            @PathVariable Long id,
            @RequestBody UpdateMeetingRequest req,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var updated = meetingService.updateMeeting(id, req, currentUser.getId());
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(updated));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<MeetingResponseDto>> getHistory(
            @AuthenticationPrincipal AuthUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @PageableDefault(size=10, sort="date", direction=Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            logger.info("🔍 GET /meetings/history called with offset={}, limit={}", offset, limit);
            
            if (currentUser == null) {
                logger.error("❌ No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            Page<Meeting> meetings;
            
            if (offset > 0 || limit != 10) {
                meetings = meetingService.getHistoryMeetings(currentUser.getId(), offset, limit);
            } else {
                meetings = meetingService.getHistoryMeetings(currentUser.getId(), pageable);
            }
            
            Page<MeetingResponseDto> response = meetings.map(MeetingResponseDto::fromEntity);
            logger.info("✅ Returning {} history meetings (total: {})", 
                       response.getNumberOfElements(), response.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings/history: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/update/series/{recurrenceId}")
    public ResponseEntity<Void> updateSeries(
            @PathVariable String recurrenceId,
            @RequestBody UpdateMeetingRequest req
    ) {
        meetingService.updateSeries(recurrenceId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MeetingResponseDto>> getAll() {
        try {
            logger.info("🔍 GET /meetings called");
            List<Meeting> meetings = meetingService.getAllMeetings();
            List<MeetingResponseDto> response = meetings.stream()
                    .map(MeetingResponseDto::fromEntity)
                    .toList();
            logger.info("✅ Returning {} meetings", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/expanded-range")
    public ResponseEntity<Page<MeetingResponseDto>> getExpandedMeetingsInRange(
            @AuthenticationPrincipal AuthUserPrincipal currentUser,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int limit
    ) {
        try {
            logger.info("🚀 GET /meetings/expanded-range called with offset={}, limit={}", offset, limit);
            logger.info("  - currentUser: {}", currentUser != null ? currentUser.getId() : "null");
            logger.info("  - from: {}, to: {}", from, to);

            if (currentUser == null) {
                logger.error("❌ No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            long startTime = System.currentTimeMillis();
            
            Page<Meeting> expandedMeetings = meetingService.getExpandedMeetingsInRange(
                currentUser.getId(), from, to, offset, limit
            );
            
            Page<MeetingResponseDto> response = expandedMeetings.map(MeetingResponseDto::fromEntity);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Returning {} expanded meetings (total: {}) in {}ms", 
                       response.getNumberOfElements(), response.getTotalElements(), duration);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings/expanded-range: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/range")
    public ResponseEntity<Page<MeetingResponseDto>> getMeetingsInRange(
            @AuthenticationPrincipal AuthUserPrincipal currentUser,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "100") int size, 
            Pageable pageable
    ) {
        try {
            logger.info("🔍 GET /meetings/range called with offset={}, limit={}", offset, limit);
            logger.info("  - currentUser: {}", currentUser != null ? currentUser.getId() : "null");
            logger.info("  - from: {}, to: {}", from, to);

            if (currentUser == null) {
                logger.error("❌ No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            Page<Meeting> page;
            
            if (offset > 0 || limit != 15) {
                page = meetingService.getMeetingsInRange(currentUser.getId(), from, to, offset, limit);
            } else {
                page = meetingService.getMeetingsInRange(currentUser.getId(), from, to, size, pageable);
            }
            
            Page<MeetingResponseDto> response = page.map(MeetingResponseDto::fromEntity);
            
            logger.info("✅ Returning {} meetings in range (total: {})", 
                       response.getNumberOfElements(), response.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings/range: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<Page<MeetingResponseDto>> getTodayMeetings(
            @AuthenticationPrincipal AuthUserPrincipal currentUser,
            @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable
    ) {
        try {
            logger.info("🔍 GET /meetings/today called");
            logger.info("  - currentUser: {}", currentUser != null ? currentUser.getId() : "null");

            if (currentUser == null) {
                logger.error("❌ No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            Page<Meeting> meetings = meetingService.getTodayMeetingsForUser(currentUser.getId(), pageable);
            Page<MeetingResponseDto> response = meetings.map(MeetingResponseDto::fromEntity);
            
            logger.info("✅ Returning {} today's meetings", response.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings/today: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<MeetingResponseDto>> getUpcomingMeetings(
            @AuthenticationPrincipal AuthUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable
    ) {
        try {
            logger.info("🔍 GET /meetings/upcoming called with offset={}, limit={}", offset, limit);
            logger.info("  - currentUser: {}", currentUser != null ? currentUser.getId() : "null");

            if (currentUser == null) {
                logger.error("❌ No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            Page<Meeting> meetings;
            
            if (offset > 0 || limit != 10) {
                meetings = meetingService.getUpcomingMeetings(currentUser.getId(), offset, limit);
            } else {
                meetings = meetingService.getUpcomingMeetings(currentUser.getId(), pageable);
            }
            
            Page<MeetingResponseDto> response = meetings.map(MeetingResponseDto::fromEntity);
            
            logger.info("✅ Returning {} upcoming meetings (total: {})", 
                       response.getNumberOfElements(), response.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in GET /meetings/upcoming: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}