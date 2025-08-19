package com.hertssu.Committee;

import com.hertssu.Committee.dto.SubcommitteeDTO;
import com.hertssu.Subcommittee.SubcommitteeRepository;
import com.hertssu.Committee.dto.CommitteeDTO;
import com.hertssu.model.Committee;
import com.hertssu.model.Subcommittee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitteeService {
    
    private final CommitteeRepository committeeRepository;
    private final SubcommitteeRepository subcommitteeRepository;
    
    public List<CommitteeDTO> getCommitteesWithSubcommittees() {
        List<Committee> committees = committeeRepository.findAll();
        
        return committees.stream()
                .map(committee -> {
                    List<Subcommittee> subcommittees = subcommitteeRepository.findByCommittee(committee);
                    List<SubcommitteeDTO> subcommitteeDTOs = subcommittees.stream()
                            .map(this::mapToSubcommitteeDTO)
                            .collect(Collectors.toList());
                    
                    return new CommitteeDTO(
                            committee.getId(),
                            committee.getSlug(),
                            committee.getName(),
                            subcommitteeDTOs
                    );
                })
                .collect(Collectors.toList());
    }
    
    private SubcommitteeDTO mapToSubcommitteeDTO(Subcommittee subcommittee) {
        return new SubcommitteeDTO(
                subcommittee.getId(),
                subcommittee.getSlug(),
                subcommittee.getName()
        );
    }
}