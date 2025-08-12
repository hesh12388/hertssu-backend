package com.hertssu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hertssu.model.User;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("select u.id from User u where u.committee.id = :committeeId")
    List<Long> idsByCommittee(@Param("committeeId") Integer committeeId);

    @Query("select u.id from User u where u.committee.id = :committeeId and upper(u.role) in :roles")
    List<Long> idsByCommitteeAndRoles(@Param("committeeId") Integer committeeId,
                                        @Param("roles") Collection<String> roles);

    @Query("select u.id from User u where u.subcommittee.id = :subId and upper(u.role) in :roles")
    List<Long> idsBySubcommitteeAndRoles(@Param("subId") Integer subcommitteeId,
                                            @Param("roles") Collection<String> roles);
}
