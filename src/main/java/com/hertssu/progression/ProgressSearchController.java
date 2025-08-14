// progression/ProgressSearchController.java
package com.hertssu.progression;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hertssu.progression.dto.UserCardDto;
import com.hertssu.security.AuthUserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressSearchController {
  private final ProgressSearchService service;

  @GetMapping("/users")
  public List<UserCardDto> searchUsers(
      @AuthenticationPrincipal AuthUserPrincipal me,
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(required = false) Integer committeeId
  ) {
    return service.searchUsersVisibleTo(me, q, committeeId);
  }
}
