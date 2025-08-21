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
    name = "meetings"
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
    private String joinUrl;
    private String zoomMeetingId;
    @ManyToOne
    private User createdBy;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingEvaluation> evaluations;
}
