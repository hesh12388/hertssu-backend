// com.hertssu.warnings.WarningRepository
package com.hertssu.warnings;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hertssu.model.Warning;

public interface WarningRepository extends JpaRepository<Warning, Long> {

  @Query("""
    select w from Warning w
    where w.user.id = :userId
    order by w.loggedAt desc
  """)
  public Page<Warning> historyForUser(
      @Param("userId") Long userId,
      Pageable pageable
  );
}
