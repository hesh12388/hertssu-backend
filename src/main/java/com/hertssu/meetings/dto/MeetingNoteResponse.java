package com.hertssu.meetings.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeetingNoteResponse {
    private Long id;
    private String note;
    private String createdAt;
    private String author; // author name
}
