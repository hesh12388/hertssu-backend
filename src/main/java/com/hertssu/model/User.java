package com.hertssu.model;

import java.time.Instant;

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

    @Column(name = "microsoft_access_token", columnDefinition = "TEXT")
    private String microsoftAccessToken;

    @Column(name = "microsoft_refresh_token", columnDefinition = "TEXT")
    private String microsoftRefreshToken;

    @Column(name = "microsoft_user_id")
    private String microsoftUserId;

    @Column(name = "microsoft_token_expires_at")
    private Instant microsoftTokenExpiresAt;
}

