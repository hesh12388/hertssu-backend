package com.hertssu.user;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.dto.AssignableUser;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final HierarchyService hierarchyService;
    private final UserRepository userRepository;
    
    @GetMapping("/assignable")
    public ResponseEntity<List<AssignableUser>> getAssignableUsers(
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        
        User currentUser = userRepository.getReferenceById(principal.getId());
        List<User> users = hierarchyService.getAllBelow(currentUser);
        
        List<AssignableUser> assignableUsers = users.stream()
                .map(AssignableUser::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(assignableUsers);
    }
}