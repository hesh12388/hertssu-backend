package com.hertssu.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.hertssu.Proposals.dto.CrossCommitteeRequestStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "cross_committee_requests")
public class CrossCommitteeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Subcommittee requester;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_committee", nullable = false)
    private Committee targetCommittee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrossCommitteeRequestStatus status = CrossCommitteeRequestStatus.IN_PROGRESS;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "crossCommitteeRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CrossCommitteeRequestComment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "crossCommitteeRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CrossCommitteeRequestDocument> documents = new ArrayList<>();
}
