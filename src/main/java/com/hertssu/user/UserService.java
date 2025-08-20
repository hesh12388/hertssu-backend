package com.hertssu.user;

import com.hertssu.Committee.CommitteeRepository;
import com.hertssu.model.AccountRequest;
import com.hertssu.model.Committee;
import com.hertssu.model.Subcommittee;
import com.hertssu.model.Task;
import com.hertssu.model.TaskComment;
import com.hertssu.model.TaskDocument;
import com.hertssu.model.User;
import com.hertssu.model.UserSupervisor;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.Subcommittee.SubcommitteeRepository;
import com.hertssu.Tasks.TaskCommentRepository;
import com.hertssu.Tasks.TaskDocumentRepository;
import com.hertssu.Tasks.TaskRepository;
import com.hertssu.Warnings.WarningRepository;
import com.hertssu.user.dto.AccountRequestDTO;
import com.hertssu.user.dto.CreateUserRequest;
import com.hertssu.user.dto.UserCardDto;
import com.hertssu.user.dto.UserResponse;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;

import com.hertssu.interview.AccountRequestRepository;
import com.hertssu.meetings.repository.MeetingEvaluationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hertssu.hierarchy.*;
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
    private final EntityManager em;
    private final HierarchyService hierarchy;
    
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

        User supervisor = null;
        if (createUserRequestDTO.getSupervisorId() != null) {
            supervisor = userRepository.getReferenceById(createUserRequestDTO.getSupervisorId());
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
        
        // If supervisor is provided, create relationship
        if (supervisor != null) {
            UserSupervisor userSupervisor = new UserSupervisor();
            userSupervisor.setUser(savedUser);
            userSupervisor.setSupervisor(supervisor);
            userSupervisorRepository.save(userSupervisor);
        }
    
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
        
        userRepository.delete(user);
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
    
    public List<UserCardDto> searchUsersVisibleTo(AuthUserPrincipal me, String q, Integer committeeFilter) {
        List<Long> allowed = hierarchy.getAllBelowIds(me.getId());
        if (allowed.isEmpty()) return List.of();

        String base =
        "select new com.hertssu.progression.dto.UserCardDto(u.id, concat(u.firstname,' ',u.lastname), u.role, u.committee.id, u.committee.name) " +
        "from User u " +
        "where u.id in :ids ";

        StringBuilder hql = new StringBuilder(base);
        if (q != null && !q.isBlank()) {
        hql.append("and (upper(u.firstname) like :q or upper(u.lastname) like :q or upper(u.email) like :q) ");
        }
        if (committeeFilter != null) {
        hql.append("and u.committee.id = :cid ");
        }
        hql.append("order by upper(u.firstname) asc, upper(u.lastname) asc");

        TypedQuery<UserCardDto> query = em.createQuery(hql.toString(), UserCardDto.class)
            .setParameter("ids", allowed);

        if (q != null && !q.isBlank()) {
        query.setParameter("q", "%" + q.trim().toUpperCase() + "%");
        }
        if (committeeFilter != null) {
        query.setParameter("cid", committeeFilter);
        }
        query.setMaxResults(50);
        return query.getResultList();
    }
}