package com.hertssu.model;

import com.hertssu.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meeting_evaluations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeetingEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    private Integer performance;
    private Integer communication;
    private Integer teamwork;
    private String notes;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
