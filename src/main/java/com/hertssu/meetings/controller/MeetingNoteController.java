package com.hertssu.meetings.controller;

import com.hertssu.meetings.dto.MeetingNoteUpdateRequest;
import com.hertssu.meetings.dto.MeetingNoteResponse;
import com.hertssu.meetings.service.MeetingNoteService;
import com.hertssu.security.AuthUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings/{meetingId}/notes")
@RequiredArgsConstructor
public class MeetingNoteController {

    private final MeetingNoteService noteService;

    @PostMapping
    public ResponseEntity<MeetingNoteResponse> addNote(
            @PathVariable Long meetingId,
            @RequestBody MeetingNoteUpdateRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        var saved = noteService.addNote(meetingId, request.getNote(), currentUser);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<MeetingNoteResponse>> getNotes(@PathVariable Long meetingId) {
        return ResponseEntity.ok(noteService.getNotes(meetingId));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<MeetingNoteResponse> updateNote(
            @PathVariable Long noteId,
            @RequestBody MeetingNoteUpdateRequest request,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(noteService.updateNote(noteId, request.getNote(), currentUser));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal AuthUserPrincipal currentUser
    ) {
        noteService.deleteNote(noteId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
