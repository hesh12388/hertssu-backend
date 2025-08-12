package com.hertssu.hertssu;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.interview.InterviewRepository;
import com.hertssu.interview.InterviewService;
import com.hertssu.interview.dto.InterviewLogRequest;
import com.hertssu.interview.dto.InterviewScheduleRequest;
import com.hertssu.interview.dto.InterviewUpdateRequest;
import com.hertssu.model.Interview;
import com.hertssu.model.User;
import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.user.UserRepository;
import com.hertssu.utils.TeamsMeetingService;
import com.hertssu.utils.dto.MeetingResponse;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

  @Mock TeamsMeetingService teams;
  @Mock InterviewRepository interviews;
  @Mock UserRepository users;
  @Mock HierarchyService hierarchy;

  @InjectMocks InterviewService service;

    private AuthUserPrincipal me() {
        return new AuthUserPrincipal((long) 1, "me@su.com", "Me", "LEADER", 10, "HR", 101, "HR_RECRUIT");
    }

    private static MeetingResponse mr(String id, String joinUrl) {
        var r = new MeetingResponse();
        r.setId(id);

        var om = new MeetingResponse.OnlineMeeting();
        om.setJoinUrl(joinUrl);
        r.setOnlineMeeting(om);

        var tb = new MeetingResponse.TimeBlock();
        tb.setDateTime(java.time.LocalDateTime.parse("2025-08-10T12:00:00"));
        tb.setTimeZone("UTC");
        r.setStart(tb);
        r.setEnd(tb);
        return r;
    }


    @Test
    void create_schedulesTeams_andSaves() {
        var me = me();
        var interviewerRef = new User(); 
        interviewerRef.setId(me.getId()); 
        interviewerRef.setEmail(me.getEmail());

        when(users.getReferenceById(me.getId())).thenReturn(interviewerRef);
        var meeting = mr("EV1",  "https://teams/join/xyz");
    
        when(teams.createMeeting(any(), any(), any(), any(), any())).thenReturn(meeting);
        when(interviews.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new InterviewScheduleRequest("Jane", "jane@gaf.com", "0100", "GAF123", "Member","HR", "Recruiting",
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30));
        var out = service.scheduleInterview(req, me);

        verify(teams).createMeeting(any(), any(), any(), any(), any());
        verify(interviews).save(argThat(i -> "EV1".equals(i.getTeamsMeetingId())));
    }

    @Test
    void create_dbFails_cancelsTeams() {
        var me = me();
        when(users.getReferenceById(me.getId())).thenReturn(new User());
        var m = mr("EV1", "https://teams/join/xyz");

        when(teams.createMeeting(any(), any(), any(), any(), any())).thenReturn(m);
        when(interviews.save(any())).thenThrow(new RuntimeException("DB"));
        
        var req = new InterviewScheduleRequest("Jane", "jane@gaf.com", "0100", "GAF123", "Member","HR", "Recruiting",
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30));
        assertThrows(RuntimeException.class, () -> service.scheduleInterview(req, me));
        verify(teams).cancelMeeting("EV1", any());
    }

    @Test
    void update_patchesTeams_andSaves() {
        var me = me();
        var existing = Interview.builder()
        .id(UUID.randomUUID())
        .name("Test-name")
        .gafEmail("cand@x.com")
        .phoneNumber("test-number")
        .gafId("test-id")
        .position("test-position")
        .committee("test-committee")
        .subCommittee("test-subcommittee")
        .teamsMeetingId("EV1")
        .startTime(LocalDateTime.now().plusDays(1))
        .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
        .build();
        
        when(interviews.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(interviews.save(any())).thenAnswer(inv -> inv.getArgument(0));
        

        var req = new InterviewUpdateRequest("updated-name", "cand@x.com", "test-number", "test-id", "test-position", "test-committee", "EV1", existing.getStartTime().plusDays(2), existing.getEndTime().plusDays(2));

        service.updateInterview(existing.getId(), req, me);

        verify(teams).updateMeeting(eq("EV1"), any(), any(), any(), any(), any());
        verify(interviews).save(argThat(i -> i.getName().equals("updated-name")));
    }


    @Test
    void delete_cancelsTeams_thenDeletes() {
        var interview = Interview.builder().id(UUID.randomUUID()).teamsMeetingId("EV1").build();
        when(interviews.findById(interview.getId())).thenReturn(Optional.of(interview));
        service.deleteInterview(interview.getId(), me());

        verify(teams).cancelMeeting("EV1", any());
        verify(interviews).deleteById(interview.getId());
    }

    @Test
    void log_updatesFields_andSaves() {
        var id = UUID.randomUUID();
        var interview = new Interview(); interview.setId(id);
        when(interviews.findById(id)).thenReturn(Optional.of(interview));

        var req = new InterviewLogRequest(5, 5, 5, 5, 5, true, "");

        service.logInterview(id, req);

        verify(interviews).save(interview);
        assertEquals(5, interview.getPerformance());
    }

    @Test
    void getInterviews_includesSelf_andAllBelow() {
        var me = me();
        var meUser = new User(); 
        meUser.setId(me.getId());
        when(users.getReferenceById(me.getId())).thenReturn(meUser);

        var uA = new User(); uA.setId((long) 2);
        var uB = new User(); uB.setId((long) 3);
        when(hierarchy.getAllBelow(meUser)).thenReturn(new ArrayList<>(List.of(uA, uB)));

        var i1 = Interview.builder().interviewer(meUser).build();
        var i2 = Interview.builder().interviewer(uA).build();
        var i3 = Interview.builder().interviewer(uB).build();
        when(interviews.findByInterviewerIn(List.of(uA,uB,meUser)))
            .thenReturn(List.of(i1,i2,i3));

        var out = service.getMyInterviews(me);
        assertEquals(out.size(), 3);
    }
}
