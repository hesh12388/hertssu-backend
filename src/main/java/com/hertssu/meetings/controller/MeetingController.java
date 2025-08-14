package com.hertssu.meetings.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final MeetingService meetingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Meeting> create(
        @RequestBody Meeting meeting,
        @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        User creator = userRepository.findById(currentUser.getId())
                                    .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(meetingService.createMeeting(meeting, creator));
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> getAll() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Meeting> update(@PathVariable Long id, @RequestBody Meeting meeting) {
        return ResponseEntity.ok(meetingService.updateMeeting(id, meeting));
    }

    @PostMapping("/add_file/{id}")
    public ResponseEntity<Void> addFile(@PathVariable Long id, @RequestBody String filePath) {
        meetingService.addFile(id, filePath);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete_file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id, @RequestBody String filePath) {
        meetingService.deleteFile(id, filePath);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today")
    public ResponseEntity<Page<Meeting>> getTodayMeetings(
        @AuthenticationPrincipal AuthUserPrincipal currentUser,
        @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(meetingService.getTodayMeetingsForUser(currentUser.getId(), pageable));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<Meeting>> getUpcomingMeetings(
        @AuthenticationPrincipal AuthUserPrincipal currentUser,
        @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(meetingService.getUpcomingMeetings(currentUser.getId(), pageable));
    }

    @GetMapping("/range")
    public ResponseEntity<Page<Meeting>> getMeetingsInRange(
        @AuthenticationPrincipal AuthUserPrincipal currentUser,
        @RequestParam String from,
        @RequestParam String to,
        @RequestParam(defaultValue = "100") int size,
        Pageable pageable
    ) {
        System.out.println("Fetching meetings from " + from + " to " + to + " for user ID: " + currentUser.getId());
        return ResponseEntity.ok(meetingService.getMeetingsInRange(currentUser.getId(), from, to, size, pageable));
    }
}
