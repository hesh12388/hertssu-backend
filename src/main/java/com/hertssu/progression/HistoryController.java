// progression/HistoryController.java
package com.hertssu.progression;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hertssu.security.AuthUserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/progress/history")
@RequiredArgsConstructor
public class HistoryController {
  private final VisibilityService visibility;
  private final HistoryService history;

    @GetMapping("/meetings")
    public Page<?> meetings(
        @AuthenticationPrincipal AuthUserPrincipal me,
        Pageable pageable
    ) {
        return history.completedMeetings(me.getId(), pageable);
    }


//   @GetMapping("/tasks/{userId}")
//   public Page<?> tasks(
//       @AuthenticationPrincipal AuthUserPrincipal me,
//       @PathVariable Long userId,
//       Pageable pageable
//   ) {
//     if (!visibility.visibleUserIds(me).contains(userId)) return Page.empty(pageable);
//     return history.completedTasks(userId, pageable);
//   }

    @GetMapping("/warnings")
    public Page<?> warnings(
        @AuthenticationPrincipal AuthUserPrincipal me,
        Pageable pageable
    ) {
        return history.loggedWarnings(me.getId(), pageable);
    }
}
