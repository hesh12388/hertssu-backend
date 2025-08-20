package com.hertssu.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name= "firstname", nullable = false)
    private String firstName;

    @Column(name= "lastname", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcommittee_id", nullable = true)
    private Subcommittee subcommittee;

    // Cascade delete for UserSupervisor where this user is the "user"
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSupervisor> userSupervisors = new ArrayList<>();

    // Cascade delete for UserSupervisor where this user is the "supervisor"
    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSupervisor> supervisedUsers = new ArrayList<>();

    // Cascade delete for Task where this user is the "assigner"
    @OneToMany(mappedBy = "assigner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> assignedTasks = new ArrayList<>();

    // Cascade delete for Task where this user is the "assignee"
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Cascade delete for Warning
    @OneToMany(mappedBy = "assigner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warning> warningAssigners = new ArrayList<>();

     @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warning> warningAssignees = new ArrayList<>();

    // Cascade delete for ProposalComment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalComment> proposalComments = new ArrayList<>();

    // Cascade delete for ProposalDocument
    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalDocument> proposalDocuments = new ArrayList<>();

    // Cascade delete for CrossCommitteeRequestComment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrossCommitteeRequestComment> crossCommitteeRequestComments = new ArrayList<>();

    // Cascade delete for CrossCommitteeRequestDocument
    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrossCommitteeRequestDocument> crossCommitteeRequestDocuments = new ArrayList<>();

    // Cascade delete for Interview
    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interview> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interview> interviewSupervisors = new ArrayList<>();
}

