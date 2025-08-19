package com.hertssu.Committee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeDTO {
    private Integer id;
    private String slug;
    private String name;
    private List<SubcommitteeDTO> subcommittees;
}