package com.hertssu.meetings.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hertssu.meetings.model.Meeting;
import com.hertssu.meetings.model.User;
import com.hertssu.meetings.repository.MeetingRepository;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private MeetingService meetingService;

    private final Meeting sampleMeeting = Meeting.builder()
            .meetingId(1)
            .title("Strategy Meeting")
            .date(LocalDate.now())
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(11, 0))
            .createdAt(LocalDateTime.now())
            .notes("Discuss goals")
            .type("Planning")
            .files(new ArrayList<>(List.of("http://example.com/doc.pdf")))
            .creator(User.builder().id(1).build())
            .build();

    @Test
    void testCreateMeeting() {
        when(meetingRepository.save(any(Meeting.class))).thenReturn(sampleMeeting);

        Meeting result = meetingService.createMeeting(sampleMeeting);

        assertEquals("Strategy Meeting", result.getTitle());
        verify(meetingRepository).save(sampleMeeting);
    }

    @Test
    void testGetAllMeetings() {
        when(meetingRepository.findAll()).thenReturn(List.of(sampleMeeting));

        List<Meeting> meetings = meetingService.getAllMeetings();

        assertEquals(1, meetings.size());
        assertEquals("Strategy Meeting", meetings.get(0).getTitle());
    }

    @Test
    void testGetMeetingById() {
        when(meetingRepository.findById(1)).thenReturn(Optional.of(sampleMeeting));

        Meeting result = meetingService.getMeetingById(1);

        assertNotNull(result);
        assertEquals(1, result.getMeetingId());
    }

    @Test
    void testDeleteMeeting() {
        meetingService.deleteMeeting(1);
        verify(meetingRepository).deleteById(1);
    }

    @Test
    void testUpdateMeeting() {
        Meeting updated = new Meeting();
        updated.setTitle("Updated");

        when(meetingRepository.findById(1)).thenReturn(Optional.of(sampleMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

        Meeting result = meetingService.updateMeeting(1, updated);

        assertEquals("Updated", result.getTitle());
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void testAddFile() {
        when(meetingRepository.findById(1)).thenReturn(Optional.of(sampleMeeting));

        meetingService.addFile(1, "http://example.com/extra.pdf");

        assertTrue(sampleMeeting.getFiles().contains("http://example.com/extra.pdf"));
    }

    @Test
    void testDeleteFile() {
        when(meetingRepository.findById(1)).thenReturn(Optional.of(sampleMeeting));

        meetingService.deleteFile(1, "http://example.com/doc.pdf");

        assertFalse(sampleMeeting.getFiles().contains("http://example.com/doc.pdf"));
    }

    @Test
    void testGetTodayMeetingsForUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Meeting> page = new PageImpl<>(List.of(sampleMeeting));

        when(meetingRepository.findByDateAndCreatorId(LocalDate.now(), 1, pageable)).thenReturn(page);

        Page<Meeting> result = meetingService.getTodayMeetingsForUser(1, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetUpcomingMeetings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Meeting> page = new PageImpl<>(List.of(sampleMeeting));

        when(meetingRepository.findByDateAfterAndCreatorId(any(), eq(1), eq(pageable))).thenReturn(page);

        Page<Meeting> result = meetingService.getUpcomingMeetings(1, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchMeetings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Meeting> page = new PageImpl<>(List.of(sampleMeeting));

        when(meetingRepository.findByTitleContainingIgnoreCase("strategy", pageable)).thenReturn(page);

        Page<Meeting> result = meetingService.searchMeetings("strategy", pageable);

        assertEquals(1, result.getTotalElements());
    }
}
