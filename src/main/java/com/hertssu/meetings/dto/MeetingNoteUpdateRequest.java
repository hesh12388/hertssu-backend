package com.hertssu.meetings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MeetingNoteUpdateRequest {
    @NotBlank
    @Size(max = 5000)
    private String note;
}
