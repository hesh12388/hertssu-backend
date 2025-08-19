package com.hertssu.profile.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hertssu.Tasks.dto.Priority;
import com.hertssu.Tasks.dto.TaskStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskSummary {
    private Long id;
    private String title;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private String assignerName;
}

