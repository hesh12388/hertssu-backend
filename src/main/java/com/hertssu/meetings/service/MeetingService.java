package com.hertssu.meetings.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.TeamsMeetingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final TeamsMeetingService teamsMeetingService;
    private final UserRepository userRepository;

    public Meeting createMeeting(Meeting meeting, User creator) {
        meeting.setCreatedBy(creator);
        meeting.setMeetingStatus("SCHEDULED");
        meeting.setCreatedAt(LocalDateTime.now());

        String startIso = meeting.getDate().atTime(meeting.getStartTime()).toString();
        String endIso = meeting.getDate().atTime(meeting.getEndTime()).toString();

        String[] participantEmails = meeting.getParticipants()
                                            .stream()
                                            .map(User::getEmail)
                                            .toArray(String[]::new);

        var response = teamsMeetingService.createMeeting(
            meeting.getTitle(),
            startIso,
            endIso,
            participantEmails,
            creator
        );

        if (response != null) {
            meeting.setTeamsEventId(response.getId());
            meeting.setJoinUrl(response.getOnlineMeeting() != null 
                ? response.getOnlineMeeting().getJoinUrl() 
                : null);
        }

        return meetingRepository.save(meeting);
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));
    }

    public void deleteMeeting(Long id) {
        meetingRepository.deleteById(id);
    }

    public Meeting updateMeeting(Long id, Meeting updatedMeeting) {
        Meeting existing = getMeetingById(id);
        updatedMeeting.setMeetingId(existing.getMeetingId());
        return meetingRepository.save(updatedMeeting);
    }

    public void addFile(Long id, String filePath) {
        Meeting meeting = getMeetingById(id);
        meeting.getFiles().add(filePath);
        meetingRepository.save(meeting);
    }

    public void deleteFile(Long id, String filePath) {
        Meeting meeting = getMeetingById(id);
        meeting.getFiles().remove(filePath);
        meetingRepository.save(meeting);
    }
    public Page<Meeting> getTodayMeetingsForUser(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        User user = userRepository.getReferenceById(userId);
        return meetingRepository.findByDateAndCreatedBy(today, user, pageable);
    }

    public Page<Meeting> getUpcomingMeetings(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        User user = userRepository.getReferenceById(userId);
        return meetingRepository.findByDateAfterAndCreatedBy(today, user, pageable);
    }

    public Page<Meeting> getMeetingsInRange(Long userId, String from, String to, int size, Pageable pageable) {
        LocalDate startDate = LocalDate.parse(from);
        LocalDate endDate = LocalDate.parse(to);
        User user = userRepository.getReferenceById(userId);
        return meetingRepository.findByDateBetweenAndCreatedBy(startDate, endDate, user, pageable);
    }
}