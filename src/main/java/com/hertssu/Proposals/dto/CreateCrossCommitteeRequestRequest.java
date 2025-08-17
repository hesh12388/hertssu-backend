package com.hertssu.Proposals.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCrossCommitteeRequestRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;
    
    @NotNull(message = "Target committee is required")
    private int targetCommitteeId;
}