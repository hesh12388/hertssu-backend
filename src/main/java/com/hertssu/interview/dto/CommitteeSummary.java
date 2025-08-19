package com.hertssu.interview.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeSummary {
    private Integer id;
    private String slug;
    private String name;
}
