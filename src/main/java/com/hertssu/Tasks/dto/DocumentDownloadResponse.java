package com.hertssu.Tasks.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDownloadResponse {
    private String downloadUrl;
    private LocalDateTime expiresAt;
}