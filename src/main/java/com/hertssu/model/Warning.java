package com.hertssu.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import com.hertssu.warnings.dto.WarningSeverity;

@Entity
@Table(name = "warnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", nullable = false)
    @NotNull(message = "Assigner is required")
    private User assigner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    @NotNull(message = "Assignee is required")
    private User assignee;
    
    @Column(name = "issued_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime issuedDate;
    
    @Column(name = "reason", nullable = false, length = 1000)
    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
    
    @Column(name = "action_taken", length = 500)
    @Size(max = 500, message = "Action taken cannot exceed 500 characters")
    private String actionTaken;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private WarningSeverity severity;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
