package com.hertssu.user.dto;


import com.hertssu.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignableUser {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    
    // Constructor from User entity
    public AssignableUser(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
    
    // Helper method to get full name for display
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
