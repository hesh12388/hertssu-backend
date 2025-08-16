package com.hertssu.Tasks.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private UserSummaryResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}