package com.hertssu.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_supervisors")
@Data
public class UserSupervisor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;
}

