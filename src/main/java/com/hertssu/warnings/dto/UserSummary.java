package com.hertssu.Warnings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    private String email;

    private String role;

    private String committee;

    private String subcommittee;
}