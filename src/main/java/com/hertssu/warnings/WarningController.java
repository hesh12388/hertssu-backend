package com.hertssu.Warnings;

import com.hertssu.Warnings.dto.WarningRequest;
import com.hertssu.Warnings.dto.WarningResponse;
import com.hertssu.security.AuthUserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warnings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarningController {
    
    private final WarningService warningService;
    
    // Create a new warning
    @PostMapping
    public ResponseEntity<WarningResponse> createWarning(
            @Valid @RequestBody WarningRequest request,
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        WarningResponse response = warningService.createWarning(request, me.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get all warnings (admin only)
    @GetMapping
    public ResponseEntity<List<WarningResponse>> getAllWarnings() {
        List<WarningResponse> warnings = warningService.getAllWarnings();
        return ResponseEntity.ok(warnings);
    }
    
    // Get warnings for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WarningResponse>> getWarningsForUser(@PathVariable Long userId) {
        List<WarningResponse> warnings = warningService.getWarningsForUser(userId);
        return ResponseEntity.ok(warnings);
    }
    
    // Update a warning
    @PutMapping("/{warningId}")
    public ResponseEntity<WarningResponse> updateWarning(
            @PathVariable Long warningId,
            @Valid @RequestBody WarningRequest request) {
        
        WarningResponse response = warningService.updateWarning(warningId, request);
        return ResponseEntity.ok(response);
    }
    
    // Delete a warning
    @DeleteMapping("/{warningId}")
    public ResponseEntity<Void> deleteWarning(@PathVariable Long warningId) {
        warningService.deleteWarning(warningId);
        return ResponseEntity.noContent().build();
    }
    
    // Get current user's warnings
    @GetMapping("/my-warnings")
    public ResponseEntity<List<WarningResponse>> getMyWarnings(
            @AuthenticationPrincipal AuthUserPrincipal me) {
        
        List<WarningResponse> warnings = warningService.getWarningsForUser(me.getId());
        return ResponseEntity.ok(warnings);
    }
}