/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.hertssu.progression.dto;
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
