/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.hertssu.meetings.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hertssu.model.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
    Page<Meeting> findByDateAndCreatorId(LocalDate date, Integer creatorId, Pageable pageable);

    Page<Meeting> findByDateAfterAndCreatorId(LocalDate now, Integer creatorId, Pageable pageable);

    Page<Meeting> findByCreator_Id(Integer userId, Pageable pageable);

    Page<Meeting> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}