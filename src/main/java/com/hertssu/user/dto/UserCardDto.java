package com.hertssu.user.dto;

import lombok.Value;

/**
 *
 * @author user
 */
@Value
public class UserCardDto {
    Long id;
    String name;
    String role;
    Integer committeeId;
    String committee;
}
