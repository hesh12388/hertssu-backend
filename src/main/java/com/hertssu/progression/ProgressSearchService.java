// progression/ProgressSearchService.java
package com.hertssu.progression;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hertssu.progression.dto.UserCardDto;
import com.hertssu.security.AuthUserPrincipal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgressSearchService {
  private final VisibilityService visibility;
  private final EntityManager em;

  public List<UserCardDto> searchUsersVisibleTo(AuthUserPrincipal me, String q, Integer committeeFilter) {
    List<Long> allowed = visibility.visibleUserIds(me);
    if (allowed.isEmpty()) return List.of();

    String base =
      "select new com.hertssu.progression.dto.UserCardDto(u.id, concat(u.firstName,' ',u.lastName), u.role, u.committee.id, u.committee.name) " +
      "from User u " +
      "where u.id in :ids ";

    StringBuilder hql = new StringBuilder(base);
    if (q != null && !q.isBlank()) {
      hql.append("and (upper(u.firstName) like :q or upper(u.lastName) like :q or upper(u.email) like :q) ");
    }
    if (committeeFilter != null) {
      hql.append("and u.committee.id = :cid ");
    }
    hql.append("order by upper(u.firstName) asc, upper(u.lastName) asc");

    TypedQuery<UserCardDto> query = em.createQuery(hql.toString(), UserCardDto.class)
        .setParameter("ids", allowed);

    if (q != null && !q.isBlank()) {
      query.setParameter("q", "%" + q.trim().toUpperCase() + "%");
    }
    if (committeeFilter != null) {
      query.setParameter("cid", committeeFilter);
    }
    query.setMaxResults(50);
    return query.getResultList();
  }
}
