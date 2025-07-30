/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.hertssu.meetings.repository;

/**
 *
 * @author user
 */

import org.springframework.data.jpa.repository.JpaRepository;

import com.hertssu.meetings.model.UserMeeting;

public interface UserMeetingRepository extends JpaRepository<UserMeeting, Long> {
}