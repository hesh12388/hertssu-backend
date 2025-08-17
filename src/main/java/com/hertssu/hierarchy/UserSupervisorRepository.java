package com.hertssu.hierarchy;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hertssu.model.UserSupervisor;
import com.hertssu.model.User;

@Repository
public interface UserSupervisorRepository extends JpaRepository <UserSupervisor, Long> {

    List<UserSupervisor> findBySupervisor(User supervisor);

    List<UserSupervisor> findByUser(User user);
    
    void deleteByUser(User user);
    void deleteBySupervisor(User supervisor);
}
