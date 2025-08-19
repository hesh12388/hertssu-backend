package com.hertssu.Committee;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hertssu.Committee.dto.CommitteeDTO;

import java.util.List;

@RestController
@RequestMapping("/committees")
@RequiredArgsConstructor
public class CommitteeController {
    
    private final CommitteeService committeeService;
    
    @GetMapping
    public ResponseEntity<List<CommitteeDTO>> getCommitteesWithSubcommittees() {
        List<CommitteeDTO> committees = committeeService.getCommitteesWithSubcommittees();
        return ResponseEntity.ok(committees);
    }
}