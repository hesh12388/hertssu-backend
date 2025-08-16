package com.hertssu.Tasks.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private UserSummaryResponse uploadedBy;
    private LocalDateTime uploadedAt;
}
