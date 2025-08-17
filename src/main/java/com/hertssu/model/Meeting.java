package com.hertssu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;


@NamedEntityGraph(
    name = "Meeting.withParticipants",
    attributeNodes = {
        @NamedAttributeNode("participants")
    }
)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "meetings",
    indexes = {
        @Index(name = "idx_meeting_recurrence", columnList = "recurrenceId"),
        @Index(name = "idx_meeting_date", columnList = "date"),
        @Index(name = "idx_meeting_status", columnList = "meetingStatus")
    }
)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    private String title;
    private String description;
    private String location;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(name = "is_all_day")
    private Boolean isAllDay;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "meeting_participants",
        joinColumns = @JoinColumn(name = "meeting_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @ElementCollection
    @CollectionTable(
        name = "meeting_reminders",
        joinColumns = @JoinColumn(name = "meeting_id")
    )
    @Column(name = "minutes_before")
    private List<Integer> reminders; 

    private String joinUrl;
    private String zoomMeetingId;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User updatedBy;

    private String meetingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;

    private String recurrenceRule; 
    private String recurrenceId;
    
    private LocalDate recurrenceUntil;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingNote> notes;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingEvaluation> evaluations;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    private LocalDateTime deletedAt;
}
