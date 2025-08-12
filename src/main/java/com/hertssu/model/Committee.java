package com.hertssu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "committees")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Committee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;
}
