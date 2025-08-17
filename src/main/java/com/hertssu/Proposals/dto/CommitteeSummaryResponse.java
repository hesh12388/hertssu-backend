package com.hertssu.Proposals.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeSummaryResponse {
    private int id;
    private String committeeName;
    private String commiteeSlug;
   
}
