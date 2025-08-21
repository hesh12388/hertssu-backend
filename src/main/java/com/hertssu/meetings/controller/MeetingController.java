package com.hertssu.meetings.controller;

import java.net.URI;
import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.MeetingResponseDto;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.service.MeetingService;
import com.hertssu.model.Meeting;
import com.hertssu.security.AuthUserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingResponseDto> create(
            @Valid @RequestBody CreateMeetingRequest req,
            @AuthenticationPrincipal AuthUserPrincipal me
    ) {
        Meeting created = meetingService.createMeeting(req, me);
        URI location = URI.create("/meetings/" + created.getMeetingId());
        return ResponseEntity.created(location).body(MeetingResponseDto.fromEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponseDto> getOne(@PathVariable Long id) {
        Meeting m = meetingService.getMeetingById(id);
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(m));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeetingResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMeetingRequest req,
            @AuthenticationPrincipal AuthUserPrincipal me
    ) {
        Meeting updated = meetingService.updateMeeting(id, req, me);
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<MeetingResponseDto>> upcoming(
            @AuthenticationPrincipal AuthUserPrincipal me,
            @PageableDefault(size = 10, sort = {"date", "startTime"}, direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<Meeting> page = meetingService.getUpcomingMeetings(me.getId(), pageable);
        return ResponseEntity.ok(page.map(MeetingResponseDto::fromEntity));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<MeetingResponseDto>> history(
            @AuthenticationPrincipal AuthUserPrincipal me,
            @PageableDefault(size = 10, sort = {"date", "startTime"}, direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Meeting> page = meetingService.getHistoryMeetings(me.getId(), pageable);
        return ResponseEntity.ok(page.map(MeetingResponseDto::fromEntity));
    }
}
