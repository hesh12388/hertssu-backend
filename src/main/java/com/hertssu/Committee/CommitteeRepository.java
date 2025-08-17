package com.hertssu.Committee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;

@Repository  
public interface CommitteeRepository extends JpaRepository<Committee, Integer> {}