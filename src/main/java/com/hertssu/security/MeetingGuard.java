package com.hertssu.security;

import com.hertssu.meetings.repository.MeetingRepository;
import com.hertssu.security.AuthUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("meetingGuard")
@RequiredArgsConstructor
public class MeetingGuard {

    private final MeetingRepository meetingRepository;

    // true if the current auth user created this meeting
    public boolean isCreator(Long meetingId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal p)) {
            return false;
        }
        return meetingRepository.findByMeetingIdAndCreatedById(meetingId, p.getId()).isPresent();
    }
}
