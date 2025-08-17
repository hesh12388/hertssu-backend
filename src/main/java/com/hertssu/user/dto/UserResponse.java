package com.hertssu.user.dto;

import com.hertssu.model.User;
import lombok.Data;

@Data
public class UserResponse{
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Integer committeeId;
    private String committeeName;
    private Integer subcommitteeId;
    private String subcommitteeName;
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.committeeId = user.getCommittee().getId();
        this.committeeName = user.getCommittee().getName();
        
        if (user.getSubcommittee() != null) {
            this.subcommitteeId = user.getSubcommittee().getId();
            this.subcommitteeName = user.getSubcommittee().getName();
        }
    }
}
