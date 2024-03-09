package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepo;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepo;
    private final Map<String, Game> games = new HashMap<>();

    public Game newGame(String id) {
        Game game = Game.builder()
                .id(id)
                .firstMove(new Random().nextInt(2) % 2 == 0 ? id.substring(0, id.indexOf("-")) : id.substring(id.indexOf("-") + 1))
                .moves(new ArrayList<>())
                .build();

        games.put(id, game);

        return game;
    }

    public Optional<Game> findPlayingGame(String id) {
        if (games.containsKey(id)) {
            return Optional.of(games.get(id));
        }

        return Optional.empty();
    }

    public void finishGame(String id) {
        Game game = games.get(id);
        var winnerDetail = (User) userDetailsService.loadUserByUsername(game.getWinner());
        var loserDetail = (User) userDetailsService.loadUserByUsername(game.getLoser());

        if (winnerDetail.getElo() > loserDetail.getElo()) {
            if (winnerDetail.getElo() - loserDetail.getElo() >= 50) {
                winnerDetail.setElo(winnerDetail.getElo() + 5);
                loserDetail.setElo(winnerDetail.getElo() - 5);
            } else {
                winnerDetail.setElo(winnerDetail.getElo() + 15);
                loserDetail.setElo(winnerDetail.getElo() - 15);
            }
        } else {
            if (loserDetail.getElo() - winnerDetail.getElo() >= 50) {
                winnerDetail.setElo(winnerDetail.getElo() + 25);
                loserDetail.setElo(winnerDetail.getElo() - 25);
            } else {
                winnerDetail.setElo(winnerDetail.getElo() + 15);
                loserDetail.setElo(winnerDetail.getElo() - 15);
            }
        }
        userRepo.save(winnerDetail);
        userRepo.save(loserDetail);

        gameRepo.save(games.get(id));
        games.remove(id);
    }
}
