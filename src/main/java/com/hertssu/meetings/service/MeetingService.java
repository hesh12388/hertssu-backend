/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.hertssu.meetings.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.model.Meeting;
import com.hertssu.model.User;

import jakarta.persistence.EntityNotFoundException;
import com.hertssu.user.UserRepository;

/**
 *
 * @author user
 */
@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    public MeetingService(MeetingRepository meetingRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    public Meeting createMeeting(Meeting meeting){
        Long creatorId = meeting.getCreator().getId();

        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + creatorId));
        
        meeting.setCreator(creator);
        return meetingRepository.save(meeting);
    }

    public List<Meeting> getAllMeetings(){
        return meetingRepository.findAll();
    }

    public Meeting getMeetingById(Integer id){
        return meetingRepository.findById(id).orElse(null);
    }

    public void deleteMeeting(Integer id){
        meetingRepository.deleteById(id);
    }

    public Meeting updateMeeting(Integer id, Meeting updatedData){
        Meeting meeting = meetingRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));        
        
        meeting.setTitle(updatedData.getTitle());
        meeting.setDate(updatedData.getDate());
        meeting.setStartTime(updatedData.getStartTime());
        meeting.setEndTime(updatedData.getEndTime());
        meeting.setNotes(updatedData.getNotes());
        meeting.setType(updatedData.getType());
        return meetingRepository.save(meeting);
    }

    public Meeting addFile(Integer meetingId, String url){
        Meeting meeting = getMeetingById(meetingId);

        meeting.getFiles().add(url);
        return meetingRepository.save(meeting);
    }

    public Meeting deleteFile(Integer meetingId, String url){
        Meeting meeting = getMeetingById(meetingId);
        if (meeting.getFiles().remove(url)) {
            return meetingRepository.save(meeting);
        } else {
            throw new EntityNotFoundException("File not found in meeting");
        }
    }

    public Page<Meeting> getTodayMeetingsForUser(Integer userId, Pageable pageable){
        return meetingRepository.findByDateAndCreatorId(LocalDate.now(), userId, pageable);
    }

    public Page<Meeting> getUpcomingMeetings(Integer userId, Pageable pageable){
        return meetingRepository.findByDateAfterAndCreatorId(LocalDate.now(), userId, pageable);
    }

    public Page<Meeting> getMeetingsByCreator(Integer userId, Pageable pageable){
        return meetingRepository.findByCreator_Id(userId, pageable);
    }

    public Page<Meeting> searchMeetings(String title, Pageable pageable){
        return meetingRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

}
