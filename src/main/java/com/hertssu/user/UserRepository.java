package com.hertssu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.Subcommittee;
import com.hertssu.model.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

   
    
    List<User> findBySubcommitteeAndRole(Subcommittee subcommittee, String role);
    List<User> findByCommitteeAndRole(Committee committee, String role);
    List<User> findByRole(String role);                                        

    List<User> findByEmailIn(List<String> emails);
}
