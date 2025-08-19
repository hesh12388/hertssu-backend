package com.hertssu.interview.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubcommitteeSummary {
    private Integer id;
    private String slug;
    private String name;
}