package com.hertssu.user;

import com.hertssu.user.dto.CreateUserRequest;
import com.hertssu.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.model.User;
import com.hertssu.profile.ProfileService;
import com.hertssu.profile.dto.UserProfileResponse;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.dto.AccountRequestDTO;
import com.hertssu.user.dto.AssignableUser;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final HierarchyService hierarchyService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProfileService profileService;
    
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

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = profileService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('PRESIDENT','VICE_PRESIDENT', 'EXECUTIVE_OFFICER','OFFICER')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequestDTO) {
        UserResponse createdUser = userService.createUser(createUserRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VICE_PRESIDENT', 'EXECUTIVE_OFFICER','OFFICER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VICE_PRESIDENT', 'EXECUTIVE_OFFICER','OFFICER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/account-requests")
    @PreAuthorize("hasAnyRole('PRESIDENT','VICE_PRESIDENT', 'EXECUTIVE_OFFICER','OFFICER')")
    public ResponseEntity<List<AccountRequestDTO>> getAccountRequests() {
        List<AccountRequestDTO> accountRequests = userService.getAllAccountRequests();
        return ResponseEntity.ok(accountRequests);
    }

    @DeleteMapping("/account-requests/{id}")
    public ResponseEntity<Void> deleteAccountRequest(@PathVariable Long id) {
        userService.deleteAccountRequest(id);
        return ResponseEntity.noContent().build();
    }
}