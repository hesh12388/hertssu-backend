package com.hertssu.utils;

import com.hertssu.model.User;
import com.hertssu.utils.dto.MeetingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamsMeetingService {

  private final GraphUserTokenProvider tokenProvider;

  @Value("${msgraph.organizer-user}")
  private String organizer;

  private final WebClient web = WebClient.builder()
      .baseUrl("https://graph.microsoft.com/v1.0")
      .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      .build();

  // CREATE (sends invites)
  public MeetingResponse createMeeting(String subject, String startIso, String endIso, String[] attendeeEmails, User me) {
    String bearer = "Bearer " + tokenProvider.getAccessToken(me);
    var body = buildEventPayload(subject, startIso, endIso, attendeeEmails);

    MeetingResponse res = web.post()
        .uri("/users/{org}/events", organizer)
        .header("Authorization", bearer)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(MeetingResponse.class)
        .block();

    return res;
  }

  // UPDATE (sends update emails)
  public void updateMeeting(String eventId, String subject, String startIso, String endIso, String[] attendeeEmails, User me) {
    String bearer = "Bearer " + tokenProvider.getAccessToken(me);
    var body = buildEventPayload(subject, startIso, endIso, attendeeEmails);
    web.patch()
        .uri(uriBuilder -> uriBuilder
            .path("/users/{org}/events/{id}")
            .queryParam("sendUpdates", "all")
            .build(organizer, eventId))
        .header("Authorization", bearer)
        .bodyValue(body)
        .retrieve()
        .toBodilessEntity()
        .block();
    }

  // CANCEL (sends cancellation emails)
  public void cancelMeeting(String eventId, User me) {
    String bearer = "Bearer " + tokenProvider.getAccessToken(me);
    var body = Map.of("comment", "Cancelled by system");

    web.post()
        .uri("/users/{org}/events/{id}/cancel", organizer, eventId)
        .header("Authorization", bearer)
        .bodyValue(body)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  // Events API payload
  private Map<String, Object> buildEventPayload(String subject, String startIso, String endIso, String[] emails) {
    return Map.of(
        "subject", subject,
        "start", Map.of("dateTime", startIso, "timeZone", "UTC"),
        "end",   Map.of("dateTime", endIso,   "timeZone", "UTC"),
        "isOnlineMeeting", true,
        "onlineMeetingProvider", "teamsForBusiness",
        "attendees", List.of(emails).stream().map(e ->
            Map.of(
              "emailAddress", Map.of("address", e, "name", e),
              "type", "required"
            )
        ).toList()
    );
  }
}
