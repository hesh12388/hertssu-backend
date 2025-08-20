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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    private Committee committee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcommittee_id")
    private Subcommittee subCommittee;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private User interviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;
   
    private Integer performance;
    private Integer experience;
    private Integer communication;
    private Integer teamwork;
    private Integer confidence;

    private Boolean accepted;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime loggedAt;

    private String meetingId;
    private String joinUrl;
    private String meetingPassword;
}