package org.one_cedrus.carobackend.user;

import java.util.List;
import java.util.Optional;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameStartingWith(String query, Pageable pageable);

    boolean existsByUsernameOrEmail(String username, String email);

    List<User> findAllByOrderByEloDesc(Pageable pageable);
}
