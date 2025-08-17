package com.hertssu.user;

import com.hertssu.Committee.CommitteeRepository;
import com.hertssu.model.AccountRequest;
import com.hertssu.model.Committee;
import com.hertssu.model.Subcommittee;
import com.hertssu.model.User;
import com.hertssu.model.UserSupervisor;
import com.hertssu.Subcommittee.SubcommitteeRepository;
import com.hertssu.user.dto.AccountRequestDTO;
import com.hertssu.user.dto.CreateUserRequest;
import com.hertssu.user.dto.UserResponse;
import com.hertssu.hierarchy.UserSupervisorRepository;
import com.hertssu.interview.AccountRequestRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final CommitteeRepository committeeRepository;
    private final SubcommitteeRepository subcommitteeRepository;
    private final UserSupervisorRepository userSupervisorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountRequestRepository accountRequestRepository;
    
    public UserResponse createUser(CreateUserRequest createUserRequestDTO) {
        // Check if user with email already exists
        if (userRepository.findByEmail(createUserRequestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + createUserRequestDTO.getEmail() + " already exists");
        }
        
        // Get committee
        Committee committee = committeeRepository.findById(createUserRequestDTO.getCommitteeId())
                .orElseThrow(() -> new RuntimeException("Committee not found with id: " + createUserRequestDTO.getCommitteeId()));
        
        // Get subcommittee if provided
        Subcommittee subcommittee = null;
        if (createUserRequestDTO.getSubcommitteeId() != null) {
            subcommittee = subcommitteeRepository.findById(createUserRequestDTO.getSubcommitteeId())
                    .orElseThrow(() -> new RuntimeException("Subcommittee not found with id: " + createUserRequestDTO.getSubcommitteeId()));
        }
        
        // Create new user
        User user = new User();
        user.setEmail(createUserRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequestDTO.getPassword()));
        user.setFirstName(createUserRequestDTO.getFirstName());
        user.setLastName(createUserRequestDTO.getLastName());
        user.setRole(createUserRequestDTO.getRole());
        user.setCommittee(committee);
        user.setSubcommittee(subcommittee);
        
        User savedUser = userRepository.save(user);
        
        // Assign supervisor based on hierarchy rules
        assignSupervisor(savedUser);
        
        return new UserResponse(savedUser);
    }
    
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UserResponse(user);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Delete supervisor relationships
        userSupervisorRepository.deleteByUser(user);
        userSupervisorRepository.deleteBySupervisor(user);
        
        userRepository.delete(user);
    }
    
    private void assignSupervisor(User user) {
        List<User> supervisors = findSupervisor(user);
        
        for (User supervisor : supervisors) {
            UserSupervisor userSupervisor = new UserSupervisor();
            userSupervisor.setUser(user);
            userSupervisor.setSupervisor(supervisor);
            userSupervisorRepository.save(userSupervisor);
        }
    }
    
    private List<User> findSupervisor(User user) {
        String role = user.getRole().toUpperCase();
        Committee committee = user.getCommittee();
        Subcommittee subcommittee = user.getSubcommittee();
        
        switch (role) {
            case "MEMBER":
                // Supervisor is associate leader of subcommittee (or leader if no associate leader)
                if (subcommittee != null) {
                    List<User> supervisors = findSubcommitteeLeader(subcommittee, "ASSOCIATE_LEADER");
                    if (supervisors == null || supervisors.isEmpty()) {
                        supervisors = findSubcommitteeLeader(subcommittee, "LEADER");
                    }
                    return supervisors;
                }
                break;
                
            case "ASSOCIATE_LEADER":
                // Supervisor is leader of same subcommittee
                if (subcommittee != null) {
                    return findSubcommitteeLeader(subcommittee, "LEADER");
                }
                break;
                
            case "LEADER":
                // Supervisor is associate chairperson (or chairperson if no associate chairperson) of same committee
                List<User> supervisors = findCommitteeChair(committee, "ASSOCIATE_CHAIRPERSON");
                if (supervisors == null || supervisors.isEmpty()) {
                    supervisors = findCommitteeChair(committee, "CHAIRPERSON");
                }
                return supervisors;
                
            case "ASSOCIATE_CHAIRPERSON":
                // Supervisor is chairperson of same committee
                return findCommitteeChair(committee, "CHAIRPERSON");
                
            case "CHAIRPERSON":
                // Supervisor is any officer
                return findByRole("OFFICER");
                
            case "OFFICER":
                // Supervisor is executive officer
                return findByRole("EXECUTIVE_OFFICER");
                
            case "EXECUTIVE_OFFICER":
                // Supervisor is vice president
                return findByRole("VICE_PRESIDENT");
                
            case "VICE_PRESIDENT":
                // Supervisor is president
                return findByRole("PRESIDENT");
                
            case "PRESIDENT":
                // No supervisor for president
                return List.of();
                
            default:
                return List.of(); // No supervisor for other roles
        }
        
        return List.of(); // Default case if no supervisor found
    }

    public List<AccountRequestDTO> getAllAccountRequests() {
        List<AccountRequest> accountRequests = accountRequestRepository.findAllByOrderByRequestedAtDesc();
        return accountRequests.stream()
                .map(AccountRequestDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteAccountRequest(Long id) {
        AccountRequest accountRequest = accountRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account request not found with id: " + id));
        accountRequestRepository.delete(accountRequest);
    }
    
    private List<User> findSubcommitteeLeader(Subcommittee subcommittee, String role) {
        return userRepository.findBySubcommitteeAndRole(subcommittee, role);
    }
    
    private List<User> findCommitteeChair(Committee committee, String role) {
        return userRepository.findByCommitteeAndRole(committee, role);
    }
    
    private List<User> findByRole(String role) {
        List<User> users = userRepository.findByRole(role);
        return users;
    }
}