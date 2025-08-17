package com.hertssu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hertssu.Tasks.dto.Priority;
import com.hertssu.Proposals.dto.ProposalStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "proposals")
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", nullable = false)
    private Committee assigner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private Subcommittee assignee;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.IN_PROGRESS;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProposalComment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProposalDocument> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CrossCommitteeRequest> crossCommitteeRequests = new ArrayList<>();
}

