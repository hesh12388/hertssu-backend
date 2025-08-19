package com.hertssu.Subcommittee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.Subcommittee;

@Repository
public interface SubcommitteeRepository extends JpaRepository<Subcommittee, Integer> {
    Subcommittee findByName(String name);
    List<Subcommittee> findByCommittee(Committee committee);
}
