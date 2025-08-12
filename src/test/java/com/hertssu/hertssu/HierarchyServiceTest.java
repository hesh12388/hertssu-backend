package com.hertssu.hertssu;

import com.hertssu.hierarchy.HierarchyService;
import com.hertssu.model.User;
import com.hertssu.model.UserSupervisor;
import com.hertssu.hierarchy.UserSupervisorRepository; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HierarchyServiceTest {

  @Mock
  UserSupervisorRepository repo;

  @InjectMocks
  HierarchyService service;

  // ----- helpers -----
  private static User u(Long id) {
    User x = new User();
    x.setId(id);
    return x;
  }
  
  private static UserSupervisor link(User sup, User child) {
    UserSupervisor us = new UserSupervisor();
    us.setSupervisor(sup);
    us.setUser(child);
    return us;
    }

  @Test
  void getAllBelow_traversesMultipleLevels() {
    User boss = u((long) 1);
    User a = u((long) 2);
    User b = u((long) 3);
    User c = u((long) 4);

    // boss -> a, b
    when(repo.findBySupervisor(boss)).thenReturn(List.of(link(boss, a), link(boss, b)));
    // a -> c
    when(repo.findBySupervisor(a)).thenReturn(List.of(link(a, c)));
    // b -> none, c -> none
    when(repo.findBySupervisor(b)).thenReturn(List.of());
    when(repo.findBySupervisor(c)).thenReturn(List.of());

    var result = service.getAllBelow(boss);

    assertThat(result.stream().map(User::getId)).containsExactlyInAnyOrder((long) 2, (long) 3, (long) 4);
  }

  @Test
  void getAllBelow_returnsEmptyForLeaf() {
    User leaf = u((long) 10);
    when(repo.findBySupervisor(leaf)).thenReturn(List.of());
    var result = service.getAllBelow(leaf);
    assertThat(result).isEmpty();
  }
}
