package com.hertssu.interview;

import org.springframework.stereotype.Repository;
import com.hertssu.model.Interview;
import com.hertssu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByInterviewerIn(List<User> interviewers);
} 