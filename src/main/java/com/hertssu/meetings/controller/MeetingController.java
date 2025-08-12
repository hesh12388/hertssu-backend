/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.hertssu.meetings.controller;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hertssu.meetings.service.MeetingService;
import com.hertssu.model.Meeting;

import lombok.RequiredArgsConstructor;
/**
 *
 * @author user
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<Meeting> create(@RequestBody Meeting meeting){
        return ResponseEntity.ok(meetingService.createMeeting(meeting));
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> getAll(){
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getOne(@PathVariable Integer id){
        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Meeting> update(@PathVariable Integer id, @RequestBody Meeting meeting){
        return ResponseEntity.ok(meetingService.updateMeeting(id, meeting));
    }

    @PostMapping("/add_file/{id}")
    public ResponseEntity<Void> addFile(@PathVariable Integer id, @RequestBody String filePath){
        meetingService.addFile(id, filePath);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete_file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Integer id, @RequestBody String filePath){
        meetingService.deleteFile(id, filePath);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today/{userId}")
    public ResponseEntity<Page<Meeting>> getTodayMeetings(
        @PathVariable Integer userId, 
        @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(meetingService.getTodayMeetingsForUser(userId, pageable));
    }

    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<Page<Meeting>> getUpcomingMeetings(
        @PathVariable Integer userId,
        @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable) {
        
        return ResponseEntity.ok(meetingService.getUpcomingMeetings(userId, pageable));
    }

    @GetMapping("/search/title/{title}")
    public ResponseEntity<Page<Meeting>> searchMeetings(
        @PathVariable String title,
        @PageableDefault(size=10, sort="startTime", direction=Sort.Direction.ASC) Pageable pageable) {
        
        return ResponseEntity.ok(meetingService.searchMeetings(title, pageable));
    }
}
