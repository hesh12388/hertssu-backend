package com.hertssu.model;

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
}

