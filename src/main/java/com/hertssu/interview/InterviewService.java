package com.hertssu.interview;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.hertssu.interview.dto.InterviewScheduleRequest;
import com.hertssu.interview.dto.InterviewUpdateRequest;
import com.hertssu.interview.dto.SubcommitteeSummary;
import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.interview.dto.CommitteeSummary;
import com.hertssu.interview.dto.InterviewLogRequest;
import com.hertssu.interview.dto.InterviewResponse;
import com.hertssu.model.AccountRequest;
import com.hertssu.model.Committee;
import com.hertssu.model.Interview;
import com.hertssu.model.Subcommittee;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.ZoomMeetingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.hertssu.utils.dto.ZoomMeetingResponse;
import com.hertssu.Committee.CommitteeRepository;
import com.hertssu.Subcommittee.SubcommitteeRepository;
@Service
@RequiredArgsConstructor
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final ZoomMeetingService teams;
    private final AccountRequestRepository accountRequestRepository;
    private final CommitteeRepository committeeRepository;
    private final SubcommitteeRepository subcommitteeRepository;


    @Transactional
    public InterviewResponse scheduleInterview(InterviewScheduleRequest req, AuthUserPrincipal me) {
        User interviewerRef = userRepository.getReferenceById(me.getId());

        Committee committee = committeeRepository.getReferenceById(req.getCommitteeId());
        Subcommittee subCommittee = null;
        if (req.getSubCommitteeId() != null) {
            subCommittee = subcommitteeRepository.getReferenceById(req.getSubCommitteeId());
        }

       
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

    
        ZoomMeetingResponse meeting  = teams.createMeeting(subject, startTimeStr, endTimeStr, attendees);
        
        // Persist interview with Teams fields
        Interview interview = Interview.builder()
            .name(req.getName())
            .gafEmail(req.getGafEmail())
            .phoneNumber(req.getPhoneNumber())
            .gafId(req.getGafId())
            .position(req.getPosition())
            .committee(committee)
            .subCommittee(subCommittee)
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .status("SCHEDULED")
            .interviewer(interviewerRef)
            .meetingId(meeting.getId())
            .joinUrl(meeting.getJoinUrl())
            .meetingPassword(meeting.getPassword())
            .build();

        // If supervisor is provided, set it
        if (req.getSupervisorId() != null) {
            User supervisor = userRepository.getReferenceById(req.getSupervisorId());
            interview.setSupervisor(supervisor);
        }

        try {
            interviewRepository.save(interview);
            return InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(mapToCommitteeSummary(interview.getCommittee()))
                        .subCommittee(mapToSubcommitteeSummary(interview.getSubCommittee()))
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
                        .supervisorName(interview.getSupervisor() != null ? interview.getSupervisor().getFirstName() : null)
                        .supervisorEmail(interview.getSupervisor() != null ? interview.getSupervisor().getEmail() : null)
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
        } 
        catch (RuntimeException ex) {
            System.out.println("Failed to save interview to database: " + ex.getMessage());
            // cancel meeting since DB failed
            try { 
                teams.cancelMeeting(meeting.getId()); 
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

        
        Interview savedInterview = interviewRepository.save(interview);

        if (Boolean.TRUE.equals(request.getAccepted())) {
            createAccountRequest(savedInterview);
        }


        return InterviewResponse.builder()
                        .id(interview.getId())
                        .name(interview.getName())
                        .gafEmail(interview.getGafEmail())
                        .phoneNumber(interview.getPhoneNumber())
                        .gafId(interview.getGafId())
                        .position(interview.getPosition())
                        .committee(mapToCommitteeSummary(interview.getCommittee()))
                        .subCommittee(mapToSubcommitteeSummary(interview.getSubCommittee()))
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
                        .supervisorName(interview.getSupervisor() != null ? interview.getSupervisor().getFirstName() : null)
                        .supervisorEmail(interview.getSupervisor() != null ? interview.getSupervisor().getEmail() : null)
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
    }

    private void createAccountRequest(Interview interview) {
        // Check if account request already exists for this interview
        if (accountRequestRepository.existsByInterviewId(interview.getId())) {
            return;
        }
        
        // Extract first name and last name from the full name
        String[] nameParts = interview.getName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
    
        AccountRequest accountRequest = AccountRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(interview.getGafEmail())
                .role(interview.getPosition()) 
                .committee(interview.getCommittee())
                .subcommittee(interview.getSubCommittee())
                .interviewId(interview.getId())
                .gafId(interview.getGafId())
                .phoneNumber(interview.getPhoneNumber())
                .requestedAt(LocalDateTime.now())
                .notes(interview.getNotes())
                .build();
        
        // If supervisor is set, assign it
        if (interview.getSupervisor() != null) {
            accountRequest.setSupervisor(interview.getSupervisor());
        }
        accountRequestRepository.save(accountRequest);
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
                        .committee(mapToCommitteeSummary(interview.getCommittee()))
                        .subCommittee(mapToSubcommitteeSummary(interview.getSubCommittee()))
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
                        .supervisorName(interview.getSupervisor() != null ? interview.getSupervisor().getFirstName() : null)
                        .supervisorEmail(interview.getSupervisor() != null ? interview.getSupervisor().getEmail() : null)
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build())
                .toList();
    }


    public void deleteInterview(UUID id, AuthUserPrincipal me) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

       
        teams.cancelMeeting(interview.getMeetingId());
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
        Committee committee = committeeRepository.getReferenceById(req.getCommitteeId());
        Subcommittee subCommittee = null;
        if (req.getSubCommitteeId() != null) {
            subCommittee = subcommitteeRepository.getReferenceById(req.getSubCommitteeId());
        }
        teams.updateMeeting(meetingId, subject, startIso, endIso, attendees);

        try{
            interview.setName(req.getName());
            interview.setGafEmail(req.getGafEmail());
            interview.setPhoneNumber(req.getPhoneNumber());
            interview.setGafId(req.getGafId());
            interview.setPosition(req.getPosition());
            interview.setCommittee(committee);
            interview.setSubCommittee(subCommittee);
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
                        .committee(mapToCommitteeSummary(interview.getCommittee()))
                        .subCommittee(mapToSubcommitteeSummary(interview.getSubCommittee()))
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
                        .supervisorName(interview.getSupervisor() != null ? interview.getSupervisor().getFirstName() : null)
                        .supervisorEmail(interview.getSupervisor() != null ? interview.getSupervisor().getEmail() : null)
                        .meetingId(interview.getMeetingId())
                        .joinUrl(interview.getJoinUrl())
                        .meetingPassword(interview.getMeetingPassword())
                        .build();
        }
        catch(Exception ex){    
             try { 
                teams.updateMeeting(meetingId, origSubject, originalStart, originalEnd, originalAttendees);
            } 
            catch (Exception ignore) {

            }
            throw ex;
        }
        
    }

    private CommitteeSummary mapToCommitteeSummary(Committee committee) {
        if (committee == null) return null;
        return new CommitteeSummary(
            committee.getId(),
            committee.getSlug(),
            committee.getName()
        );
    }

    private SubcommitteeSummary mapToSubcommitteeSummary(Subcommittee subcommittee) {
        if (subcommittee == null) return null;
        return new SubcommitteeSummary(
            subcommittee.getId(),
            subcommittee.getSlug(),
            subcommittee.getName()
        );
    }


}
