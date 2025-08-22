package com.hertssu.meetings;


import com.hertssu.meetings.dto.CreateMeetingRequest;
import com.hertssu.meetings.dto.UpdateMeetingRequest;
import com.hertssu.meetings.dto.MeetingResponse;
import com.hertssu.meetings.dto.UserResponse;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hertssu.utils.dto.ZoomMeetingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ZoomMeetingService zoomMeetingService;

    public MeetingResponse createMeeting(CreateMeetingRequest request, AuthUserPrincipal me) {
        // Get the user creating the meeting
        User creator = userRepository.getReferenceById(me.getId());
        
        // Get participants
        List<User> participants = userRepository.findAllById(request.getParticipantIds());
        if (participants.size() != request.getParticipantIds().size()) {
            throw new RuntimeException("Some participants not found");
        }
        participants.add(creator);

        // Create Zoom meeting if not all-day
        String joinUrl = null;
        String zoomMeetingId = null;
        try {
            String startIso = buildIsoString(request.getDate(), request.getStartTime());
            String endIso = buildIsoString(request.getDate(), request.getEndTime());
            
            String[] participantEmails = participants.stream()
                .map(User::getEmail)
                .toArray(String[]::new);
            
            ZoomMeetingResponse zoomResponse = zoomMeetingService.createMeeting(
                request.getTitle(), startIso, endIso, participantEmails);
            
            joinUrl = zoomResponse.getJoinUrl();
            zoomMeetingId = zoomResponse.getId().toString();
            
        } catch (Exception e) {
            log.error("Failed to create Zoom meeting", e);
            throw new RuntimeException("Failed to create Zoom meeting", e);
        }
        

        // Create and save meeting entity
        Meeting meeting = Meeting.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .location(request.getLocation())
            .date(request.getDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .isAllDay(request.getIsAllDay())
            .participants(participants)
            .joinUrl(joinUrl)
            .zoomMeetingId(zoomMeetingId)
            .createdBy(creator)
            .createdAt(LocalDateTime.now())
            .build();

        Meeting savedMeeting = meetingRepository.save(meeting);
        return convertToResponse(savedMeeting);
    }

    public MeetingResponse updateMeeting(Long meetingId, UpdateMeetingRequest request) {
 
        Meeting meeting = meetingRepository.getReferenceById(meetingId);

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setLocation(request.getLocation());
        meeting.setDate(request.getDate());
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());
        meeting.setIsAllDay(request.getIsAllDay());
       
        // Update participants if provided
        if (request.getParticipantIds() != null) {
            List<User> participants = userRepository.findAllById(request.getParticipantIds());
            if (participants.size() != request.getParticipantIds().size()) {
                throw new RuntimeException("Some participants not found");
            }
            participants.add(meeting.getCreatedBy());
            meeting.setParticipants(participants);
        }

        // Update Zoom meeting
        try {
            String startIso = buildIsoString(meeting.getDate(), meeting.getStartTime());
            String endIso = buildIsoString(meeting.getDate(), meeting.getEndTime());
            
            String[] participantEmails = meeting.getParticipants().stream()
                .map(User::getEmail)
                .toArray(String[]::new);
            
            zoomMeetingService.updateMeeting(
                meeting.getZoomMeetingId(), meeting.getTitle(), 
                startIso, endIso, participantEmails);
            
        } catch (Exception e) {
            log.error("Failed to update Zoom meeting", e);
            throw new RuntimeException("Failed to update Zoom meeting", e);
        }
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        return convertToResponse(savedMeeting);
    }

    public void deleteMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found: " + meetingId));

        // Cancel Zoom meeting if exists
        if (meeting.getZoomMeetingId() != null) {
            try {
                zoomMeetingService.cancelMeeting(meeting.getZoomMeetingId());
            } catch (Exception e) {
                log.error("Failed to cancel Zoom meeting", e);
                throw new RuntimeException("Failed to cancel Zoom meeting", e);
            }
        }
        meetingRepository.delete(meeting);
    }

    @Transactional(readOnly = true)
    public Page<MeetingResponse> getUpcomingMeetings(Pageable pageable, AuthUserPrincipal me) {
        User user = userRepository.getReferenceById(me.getId());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDate = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        Page<Meeting> meetings = meetingRepository.findUpcomingMeetingsForUser(user, currentDate, currentTime, pageable);
        return meetings.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MeetingResponse> getHistoryMeetings(Pageable pageable, AuthUserPrincipal me) {
        User user = userRepository.getReferenceById(me.getId());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDate = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        Page<Meeting> meetings = meetingRepository.findHistoryMeetingsForUser(user, currentDate, currentTime, pageable);
        return meetings.map(this::convertToResponse);
    }

    private String buildIsoString(LocalDate date, java.time.LocalTime time) {
        return OffsetDateTime.of(date, time, ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private MeetingResponse convertToResponse(Meeting meeting) {
        List<UserResponse> participantResponses = meeting.getParticipants().stream()
            .map(this::convertUserToResponse)
            .toList();

        return MeetingResponse.builder()
            .meetingId(meeting.getMeetingId())
            .title(meeting.getTitle())
            .description(meeting.getDescription())
            .location(meeting.getLocation())
            .date(meeting.getDate())
            .startTime(meeting.getStartTime())
            .endTime(meeting.getEndTime())
            .isAllDay(meeting.getIsAllDay())
            .participants(participantResponses)
            .joinUrl(meeting.getJoinUrl())
            .zoomMeetingId(meeting.getZoomMeetingId())
            .createdBy(convertUserToResponse(meeting.getCreatedBy()))
            .createdAt(meeting.getCreatedAt())
            .build();
    }

    private UserResponse convertUserToResponse(User user) {
        if (user == null) return null;
        
        return UserResponse.builder()
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}
