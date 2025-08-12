package com.hertssu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subcommittees")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Subcommittee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    private Committee committee;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;
}

