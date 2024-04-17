package org.one_cedrus.carobackend.game;

import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findGameById(Long id);

    List<Game> findGamesByUsersContainsOrderByIdDesc(User user, Pageable pageable);
}
