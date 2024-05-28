package org.one_cedrus.carobackend.game;

import java.util.List;
import java.util.Optional;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findGameById(Long id);

    List<Game> findGamesByUsersContainsOrderByIdDesc(
        User user,
        Pageable pageable
    );

    Integer countGamesByUsersContains(User user);
}
