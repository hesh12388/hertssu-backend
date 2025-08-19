package com.hertssu.Committee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubcommitteeDTO {
    private Integer id;
    private String slug;
    private String name;
}