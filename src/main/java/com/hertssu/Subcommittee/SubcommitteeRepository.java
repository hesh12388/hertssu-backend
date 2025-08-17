package com.hertssu.Subcommittee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hertssu.model.Subcommittee;

@Repository
public interface SubcommitteeRepository extends JpaRepository<Subcommittee, Integer> {}
