package com.hertssu.hierarchy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.hertssu.model.UserSupervisor;

import lombok.AllArgsConstructor;
import com.hertssu.model.User;
import com.hertssu.user.UserRepository;

@Service
@AllArgsConstructor
public class HierarchyService {
    
    private final UserSupervisorRepository userSupervisorRepository;
    private final UserRepository userRepository;

    // get those who report directly to me
    public List<User> getDirectReports(User supervisor) {
        return userSupervisorRepository.findBySupervisor(supervisor)
                                    .stream()
                                    .map(UserSupervisor::getUser)
                                    .toList();
    }

    // get direct supervisors
    public List<User> getSupervisors(User user) {
        return userSupervisorRepository.findByUser(user)
                                    .stream()
                                    .map(UserSupervisor::getSupervisor)
                                    .toList();
    }

    // get subordinates using BFS
    public List<User> getAllBelow(User supervisor) {
        List<User> result = new ArrayList<>();
        Queue<User> queue = new LinkedList<>();

        queue.addAll(getDirectReports(supervisor));

        while (!queue.isEmpty()) {
            User current = queue.poll();
            result.add(current);
            queue.addAll(getDirectReports(current));
        }

        return result;
    }

    public List<User> getAllBelowByUserId(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return getAllBelow(user);
    }
}