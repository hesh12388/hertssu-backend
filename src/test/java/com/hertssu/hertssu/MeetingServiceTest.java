package com.hertssu.hertssu;

import com.hertssu.exceptions.MeetingNotFoundException;
import com.hertssu.exceptions.ZoomSyncException;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;
import com.hertssu.meetings.repository.MeetingNoteRepository;
import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import com.hertssu.meetings.service.MeetingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ZoomMeetingService zoomMeetingService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MeetingEvaluationRepository evaluationRepository;
    @Mock
    private MeetingNoteRepository noteRepository;

    @InjectMocks
    private MeetingService meetingService;

    private Meeting sampleMeeting;
    private User creator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        creator = new User();
        creator.setId(1L);
        creator.setEmail("test@domain.com");

        sampleMeeting = new Meeting();
        sampleMeeting.setMeetingId(10L);
        sampleMeeting.setTitle("Sample");
        sampleMeeting.setDate(LocalDate.now());
        sampleMeeting.setStartTime(LocalTime.of(10, 0));
        sampleMeeting.setEndTime(LocalTime.of(11, 0));
        sampleMeeting.setCreatedBy(creator);
    }

    @Test
    void getMeetingById_found_returnsMeeting() {
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(sampleMeeting));

        Meeting m = meetingService.getMeetingById(10L);

        assertNotNull(m);
        assertEquals("Sample", m.getTitle());
        verify(meetingRepository, times(1)).findById(10L);
    }

    @Test
    void getMeetingById_notFound_throwsException() {
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MeetingNotFoundException.class, () -> meetingService.getMeetingById(99L));
    }

    @Test
    void deleteMeeting_withZoomError_throwsZoomSyncException() {
        sampleMeeting.setZoomMeetingId("zoom123");
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(sampleMeeting));
        doThrow(new RuntimeException("Zoom API down")).when(zoomMeetingService)
                .cancelMeeting(eq("zoom123"), any(User.class));

        assertThrows(ZoomSyncException.class, () -> meetingService.deleteMeeting(10L));
    }

    @Test
    void updateMeeting_success_updatesZoom() {
        sampleMeeting.setZoomMeetingId("zoom123");
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(sampleMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMeetingRequest req = new UpdateMeetingRequest();
        req.setTitle("Updated title");

        Meeting updated = meetingService.updateMeeting(10L, req, 1L);

        assertEquals("Updated title", updated.getTitle());
        verify(zoomMeetingService, times(1))
                .updateMeeting(eq("zoom123"), eq("Updated title"), anyString(), anyString(), any(String[].class), eq(creator));
    }

    @Test
    void updateMeeting_zoomFails_throwsZoomSyncException() {
        sampleMeeting.setZoomMeetingId("zoom123");
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(sampleMeeting));
        doThrow(new RuntimeException("Zoom update failed")).when(zoomMeetingService)
                .updateMeeting(anyString(), anyString(), anyString(), anyString(), any(String[].class), any(User.class));

        UpdateMeetingRequest req = new UpdateMeetingRequest();
        req.setTitle("Crash");

        assertThrows(ZoomSyncException.class, () -> meetingService.updateMeeting(10L, req, 1L));
    }
}
