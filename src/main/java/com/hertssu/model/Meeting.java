package com.hertssu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    private String title;
    private String description;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToMany
    private List<User> participants;

    private String teamsEventId;
    private String joinUrl;

    @ManyToOne
    private User createdBy;

    private String meetingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ElementCollection
    private List<String> files;
}
