// progression/VisibilityService.java
package com.hertssu.progression;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hertssu.security.AuthUserPrincipal;
import com.hertssu.security.RoleRank;
import com.hertssu.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisibilityService {
  private final UserRepository userRepository;

  public List<Long> visibleUserIds(AuthUserPrincipal me) {
    // If you want org-wide superpowers, add a check here (e.g. PRESIDENT sees all)
    int myRank = RoleRank.rankOf(me.getRole());
    Integer committeeId = me.getCommitteeId(); // make sure principal exposes this

    // roles LOWER than me
    // Build a role set by filtering RANK map
    var lowerRoles = RoleRank.RANK.entrySet().stream()
        .filter(e -> e.getValue() > myRank)
        .map(e -> e.getKey())
        .toList();

    return userRepository.idsByCommitteeAndRoles(committeeId, lowerRoles);
  }
}
