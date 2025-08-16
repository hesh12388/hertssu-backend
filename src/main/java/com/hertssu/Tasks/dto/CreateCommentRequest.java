package com.hertssu.Tasks.dto;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String content;
}