package com.hertssu.Warnings;

import com.hertssu.Warnings.dto.WarningRequest;
import com.hertssu.Warnings.dto.WarningResponse;
import com.hertssu.Warnings.dto.UserSummary;
import com.hertssu.model.Warning;
import com.hertssu.model.User;
import com.hertssu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WarningService {
    
    private final WarningRepository warningRepository;
    private final UserRepository userRepository;
    
    // Get all warnings
    public List<WarningResponse> getAllWarnings() {
        List<Warning> warnings = warningRepository.findAll();
        return warnings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Create a new warning
    public WarningResponse createWarning(WarningRequest request, Long assignerId) {
        User assigner = userRepository.getReferenceById(assignerId);
        
        User assignee = userRepository.getReferenceById(request.getAssigneeId());
        
        Warning warning = new Warning();
        warning.setAssigner(assigner);
        warning.setAssignee(assignee);
        warning.setReason(request.getReason());
        warning.setActionTaken(request.getActionTaken());
        warning.setSeverity(request.getSeverity());
        
        Warning savedWarning = warningRepository.save(warning);
        return convertToResponse(savedWarning);
    }
    
    // Get warnings for a specific user
    public List<WarningResponse> getWarningsForUser(Long userId) {
        List<Warning> warnings = warningRepository.findByAssigneeIdOrderByIssuedDateDesc(userId);
        return warnings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Update a warning
    public WarningResponse updateWarning(Long warningId, WarningRequest request) {
        Warning warning = warningRepository.findById(warningId)
                .orElseThrow(() -> new RuntimeException("Warning not found"));
        
        // Update assignee if changed
        User assignee = userRepository.getReferenceById(request.getAssigneeId());
    
        warning.setAssignee(assignee);
        warning.setReason(request.getReason());
        warning.setActionTaken(request.getActionTaken());
        warning.setSeverity(request.getSeverity());
        
        Warning updatedWarning = warningRepository.save(warning);
        return convertToResponse(updatedWarning);
    }
    
    // Delete a warning
    public void deleteWarning(Long warningId) {
        if (!warningRepository.existsById(warningId)) {
            throw new RuntimeException("Warning not found");
        }
        warningRepository.deleteById(warningId);
    }
    
    // Helper method to convert Warning entity to WarningResponse DTO
    private WarningResponse convertToResponse(Warning warning) {
        return new WarningResponse(
                warning.getId(),
                convertToUserSummary(warning.getAssigner()),
                convertToUserSummary(warning.getAssignee()),
                warning.getIssuedDate(),
                warning.getReason(),
                warning.getActionTaken(),
                warning.getSeverity(),
                warning.getCreatedAt(),
                warning.getUpdatedAt()
        );
    }
    
    // Helper method to convert User entity to UserSummary DTO
    private UserSummary convertToUserSummary(User user) {
        return new UserSummary(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.getCommittee().getName(),
            user.getSubcommittee() != null ? user.getSubcommittee().getName() : null
        );
    }
}