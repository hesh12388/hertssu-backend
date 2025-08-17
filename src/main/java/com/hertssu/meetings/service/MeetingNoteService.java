package com.hertssu.meetings.service;

import com.hertssu.meetings.dto.MeetingNoteResponse;
import com.hertssu.meetings.dto.MeetingNoteUpdateRequest;
import com.hertssu.meetings.repository.MeetingNoteRepository;
import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.MeetingNote;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException; // <- jakarta for Spring Boot 3
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingNoteService {

    private final MeetingNoteRepository noteRepo;
    private final MeetingRepository meetingRepo;
    private final UserRepository userRepo;

    @Transactional
    public MeetingNoteResponse addNote(Long meetingId, String text, AuthUserPrincipal me) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        User author = userRepo.getReferenceById(me.getId());

        MeetingNote note = MeetingNote.builder()
                .meeting(meeting)
                .author(author)
                .note(text)
                .createdAt(LocalDateTime.now())
                .build();

        MeetingNote saved = noteRepo.save(note);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingNoteResponse> getNotes(Long meetingId) {
        return noteRepo.findByMeeting_MeetingIdOrderByCreatedAtDesc(meetingId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MeetingNoteResponse updateNote(Long noteId, String text, AuthUserPrincipal me) {
        MeetingNote note = noteRepo.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
        if (!note.getAuthor().getId().equals(me.getId())) {
            throw new AccessDeniedException("You can only edit your own notes");
        }
        note.setNote(text);
        note.setUpdatedAt(LocalDateTime.now());
        return toResponse(noteRepo.save(note));
    }

    @Transactional
    public void deleteNote(Long noteId, AuthUserPrincipal me) {
        MeetingNote note = noteRepo.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
        if (!note.getAuthor().getId().equals(me.getId())) {
            throw new AccessDeniedException("You can only delete your own notes");
        }
        noteRepo.delete(note);
    }

    private MeetingNoteResponse toResponse(MeetingNote note) {
        return MeetingNoteResponse.builder()
                .id(note.getId())
                .note(note.getNote())                 // expose as `text` to match frontend
                .author(note.getAuthor().getFirstName())
                .createdAt(note.getCreatedAt() == null ? null : note.getCreatedAt().toString())
                .build();
    }
}
