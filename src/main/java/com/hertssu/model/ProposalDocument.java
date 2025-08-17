package com.hertssu.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "proposal_documents")
public class ProposalDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "s3_key", nullable = false)
    private String s3Key;
    
    @Column(name = "s3_bucket", nullable = false)
    private String s3Bucket;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "content_type")
    private String contentType;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
}