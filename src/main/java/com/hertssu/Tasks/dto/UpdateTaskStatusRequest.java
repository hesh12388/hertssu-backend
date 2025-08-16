package com.hertssu.Tasks.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}