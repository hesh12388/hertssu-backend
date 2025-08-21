package com.hertssu.meetings.dto;

import com.hertssu.model.Meeting;
import com.hertssu.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class MeetingResponseDto {
    private Long meetingId;
    private String title;
    private String description;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private boolean isAllDay;
    private List<String> participantEmails;
    private List<ParticipantLiteDto> participants;
    private String meetingStatus;
    private String createdAt;
    private String updatedAt;
    private String zoomMeetingId;
    private String joinUrl;

    public static MeetingResponseDto fromEntity(Meeting m) {
        var dateFmt = DateTimeFormatter.ISO_DATE;
        var timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        List<User> users = m.getParticipants() != null ? m.getParticipants() : Collections.emptyList();

        return MeetingResponseDto.builder()
                .meetingId(m.getMeetingId())
                .title(m.getTitle())
                .description(m.getDescription())
                .location(m.getLocation())
                .date(m.getDate() != null ? m.getDate().format(dateFmt) : null)
                .startTime(m.getStartTime() != null ? m.getStartTime().format(timeFmt) : null)
                .endTime(m.getEndTime() != null ? m.getEndTime().format(timeFmt) : null)
                .isAllDay(Boolean.TRUE.equals(m.getIsAllDay()))

                .participantEmails(
                        users.stream()
                                .map(u -> u.getEmail() != null ? u.getEmail() : "")
                                .collect(Collectors.toList())
                )

                .participants(
                        users.stream()
                                .map(u -> ParticipantLiteDto.builder()
                                        .id(u.getId() != null ? u.getId() : 0L)
                                        .email(u.getEmail() != null ? u.getEmail() : "")
                                        .firstName(u.getFirstName() != null ? u.getFirstName() : "")
                                        .lastName(u.getLastName() != null ? u.getLastName() : "")
                                        .build())
                                .collect(Collectors.toList())
                )
                .meetingStatus(m.getMeetingStatus())
                .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null)
                .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : null)
                .zoomMeetingId(m.getZoomMeetingId())
                .joinUrl(m.getJoinUrl())
                .build();
    }
}
