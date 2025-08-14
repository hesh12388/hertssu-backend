package com.hertssu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String gafEmail;

    private String phoneNumber;

    private String gafId;

    private String position;

    private String committee;
    
    private String subCommittee;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private User interviewer;

   
    private Integer performance;
    private Integer experience;
    private Integer communication;
    private Integer teamwork;
    private Integer confidence;

    private Boolean accepted;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime loggedAt;

    private String teamsMeetingId;
    private String teamsJoinUrl;
}