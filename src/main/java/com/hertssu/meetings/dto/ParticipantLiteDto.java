package com.hertssu.meetings.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantLiteDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
