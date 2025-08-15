package com.hertssu.interview;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.hertssu.interview.dto.InterviewScheduleRequest;
import com.hertssu.interview.dto.InterviewUpdateRequest;
import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.interview.dto.InterviewLogRequest;
import com.hertssu.interview.dto.InterviewResponse;
import com.hertssu.model.Interview;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.hertssu.utils.dto.MeetingResponse;
@Service
@RequiredArgsConstructor
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final ZoomMeetingService teams;


    @Transactional
    public InterviewResponse scheduleInterview(InterviewScheduleRequest req, AuthUserPrincipal me) {
        User interviewerRef = userRepository.getReferenceById(me.getId());

       
        String [] attendees = new String[]{req.getGafEmail(), me.getEmail()};

        String subject = "HR Interview for: " + req.getPosition() + " role";
        ZoneId egyptZone = ZoneId.of("Africa/Cairo");

        String startTimeStr = req.getStartTime()
            .atZone(egyptZone)
            .toOffsetDateTime()
            .toString();

        String endTimeStr = req.getEndTime()
            .atZone(egyptZone)
            .toOffsetDateTime()
            .toString();

    
        MeetingResponse meeting  = teams.createMeeting(subject, startTimeStr, endTimeStr, attendees, interviewerRef);
        
        // Persist interview with Teams fields
        Interview interview = Interview.builder()
            .name(req.getName())
            .gafEmail(req.getGafEmail())
            .phoneNumber(req.getPhoneNumber())
            .gafId(req.getGafId())
            .position(req.getPosition())
            .committee(req.getCommittee())
            .subCommittee(req.getSubCommittee())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .status("SCHEDULED")
            .interviewer(interviewerRef)
            .meetingId(meeting.getId())
            .joinUrl(meeting.getJoinUrl())
            .meetingPassword(meeting.getPassword())
            .build();

        try {
            interviewRepository.save(interview);
            return InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(interview.getCommittee())
                        .subCommittee(interview.getSubCommittee())
                        .startTime(interview.getStartTime())
                        .endTime(interview.getEndTime())
                        .status(interview.getStatus())
                        .performance(interview.getPerformance())
                        .experience(interview.getExperience())
                        .communication(interview.getCommunication())
                        .teamwork(interview.getTeamwork())
                        .confidence(interview.getConfidence())
                        .accepted(interview.getAccepted())
                        .notes(interview.getNotes())
                        .interviewerName(interview.getInterviewer().getFirstName())
                        .interviewerEmail(interview.getInterviewer().getEmail())
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
        } 
        catch (RuntimeException ex) {
            System.out.println("Failed to save interview to database: " + ex.getMessage());
            // cancel meeting since DB failed
            try { 
                teams.cancelMeeting(meeting.getId(), interviewerRef); 
            } 
            catch (Exception ignore) {

            }
            throw ex;
        }
    }

    @Transactional
    public InterviewResponse logInterview(UUID id, InterviewLogRequest request) {
        Interview interview = interviewRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

        interview.setPerformance(request.getPerformance());
        interview.setExperience(request.getExperience());
        interview.setCommunication(request.getCommunication());
        interview.setTeamwork(request.getTeamwork());
        interview.setConfidence(request.getConfidence());
        interview.setAccepted(request.getAccepted());
        interview.setNotes(request.getNotes());
        interview.setStatus("LOGGED");

        interviewRepository.save(interview);

        return InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(interview.getCommittee())
                        .subCommittee(interview.getSubCommittee())
                        .startTime(interview.getStartTime())
                        .endTime(interview.getEndTime())
                        .status(interview.getStatus())
                        .performance(interview.getPerformance())
                        .experience(interview.getExperience())
                        .communication(interview.getCommunication())
                        .teamwork(interview.getTeamwork())
                        .confidence(interview.getConfidence())
                        .accepted(interview.getAccepted())
                        .notes(interview.getNotes())
                        .interviewerName(interview.getInterviewer().getFirstName())
                        .interviewerEmail(interview.getInterviewer().getEmail())
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
    }

    
    public List<InterviewResponse> getMyInterviews(AuthUserPrincipal me) {

        User currentUser = userRepository.getReferenceById(me.getId());
        // Get everyone under me in the hierarchy
        List<User> subordinates = hierarchyService.getAllBelow(currentUser);

        // Include myself in the allowed set
        subordinates.add(currentUser);

        // Get interviews within this set
        List<Interview> interviews = interviewRepository.findByInterviewerIn(subordinates);

        return interviews.stream()
                .map(interview -> InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(interview.getCommittee())
                        .subCommittee(interview.getSubCommittee())
                        .startTime(interview.getStartTime())
                        .endTime(interview.getEndTime())
                        .status(interview.getStatus())
                        .performance(interview.getPerformance())
                        .experience(interview.getExperience())
                        .communication(interview.getCommunication())
                        .teamwork(interview.getTeamwork())
                        .confidence(interview.getConfidence())
                        .accepted(interview.getAccepted())
                        .notes(interview.getNotes())
                        .interviewerName(interview.getInterviewer().getFirstName())
                        .interviewerEmail(interview.getInterviewer().getEmail())
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build())
                .toList();
    }


    public void deleteInterview(UUID id, AuthUserPrincipal me) {
        User user = userRepository.getReferenceById(me.getId());
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

       
        teams.cancelMeeting(interview.getMeetingId(), user);
        interviewRepository.deleteById(id);
       
    }

    @Transactional
    public InterviewResponse updateInterview(UUID id, InterviewUpdateRequest req, AuthUserPrincipal me) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        ZoneId egypt = ZoneId.of("Africa/Cairo");
        String origSubject = "HR Interview for: " + interview.getPosition() + " role";
        String originalStart =  interview.getStartTime().atZone(egypt).toOffsetDateTime().toString();
        String originalEnd = interview.getEndTime().atZone(egypt).toOffsetDateTime().toString();
        String[] originalAttendees = { interview.getGafEmail(), me.getEmail()};

        String meetingId = interview.getMeetingId();
        String subject = "HR Interview for: " + req.getPosition() + " role";

        String startIso = req.getStartTime().atZone(egypt).toOffsetDateTime().toString();
        String endIso   = req.getEndTime().atZone(egypt).toOffsetDateTime().toString();

        String[] attendees = { req.getGafEmail(), me.getEmail()};
        User user = userRepository.getReferenceById(me.getId());
        teams.updateMeeting(meetingId, subject, startIso, endIso, attendees, user);

        try{
            interview.setName(req.getName());
            interview.setGafEmail(req.getGafEmail());
            interview.setPhoneNumber(req.getPhoneNumber());
            interview.setGafId(req.getGafId());
            interview.setPosition(req.getPosition());
            interview.setCommittee(req.getCommittee());
            interview.setSubCommittee(req.getSubCommittee());
            interview.setStartTime(req.getStartTime());
            interview.setEndTime(req.getEndTime());

            interviewRepository.save(interview);

            return InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(interview.getCommittee())
                        .subCommittee(interview.getSubCommittee())
                        .startTime(interview.getStartTime())
                        .endTime(interview.getEndTime())
                        .status(interview.getStatus())
                        .performance(interview.getPerformance())
                        .experience(interview.getExperience())
                        .communication(interview.getCommunication())
                        .teamwork(interview.getTeamwork())
                        .confidence(interview.getConfidence())
                        .accepted(interview.getAccepted())
                        .notes(interview.getNotes())
                        .interviewerName(interview.getInterviewer().getFirstName())
                        .interviewerEmail(interview.getInterviewer().getEmail())
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
        }
        catch(Exception ex){    
             try { 
                teams.updateMeeting(meetingId, origSubject, originalStart, originalEnd, originalAttendees, user);
            } 
            catch (Exception ignore) {

            }
            throw ex;
        }
        
    }


}
