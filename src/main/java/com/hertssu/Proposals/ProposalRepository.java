package com.hertssu.Proposals;

import org.springframework.stereotype.Repository;

import com.hertssu.model.Committee;
import com.hertssu.model.Proposal;
import com.hertssu.model.Subcommittee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByAssignee(Subcommittee assignee);
    
    List<Proposal> findByAssigner(Committee assigner); 
}
