package com.hertssu.Proposals;

import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.Proposal;
import com.hertssu.model.Subcommittee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    // Find proposals assigned to a user
    List<Proposal> findByAssignee(Subcommittee assignee);
    
    // Find proposals assigned by a user
    List<Proposal> findByAssigner(Committee assigner); 
}
