package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepo;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepo;
    private final Map<String, Game> games = new HashMap<>();
    private final Map<String, String> usernameToGame = new HashMap<>();

    public Game newGame(String id) {
        Game game = Game.builder()
                .id(id)
                .firstMove(new Random().nextInt(2) % 2 == 0 ? id.substring(0, id.indexOf("-")) : id.substring(id.indexOf("-") + 1))
                .moves(new ArrayList<>())
                .build();

        games.put(id, game);

        return game;
    }

    public void submitGame(String username, String id) {
        usernameToGame.put(username, id);
    }

    public void removeGame(String username) {
        usernameToGame.remove(username);
    }

    public String findGameByUsername(String username) {
        return usernameToGame.getOrDefault(username, "");
    }

    public Optional<Game> findPlayingGame(String id) {
        if (games.containsKey(id)) {
            return Optional.of(games.get(id));
        }

        return Optional.empty();
    }

    public void drawGame(String id) {
        Game game = games.get(id);

        usernameToGame.remove(game.firstUser());
        usernameToGame.remove(game.secondUser());

        gameRepo.save(games.get(id));
        games.remove(id);
    }

    public void finishGame(String id) {
        Game game = games.get(id);

        usernameToGame.remove(game.firstUser());
        usernameToGame.remove(game.secondUser());

        var winnerDetail = (User) userDetailsService.loadUserByUsername(game.getWinner());
        var loserDetail = (User) userDetailsService.loadUserByUsername(game.getLoser());

        if (winnerDetail.getElo() > loserDetail.getElo()) {
            if (winnerDetail.getElo() - loserDetail.getElo() >= 50) {
                winnerDetail.setElo(winnerDetail.getElo() + 5);
                loserDetail.setElo(loserDetail.getElo() - 5);
            } else {
                winnerDetail.setElo(winnerDetail.getElo() + 15);
                loserDetail.setElo(loserDetail.getElo() - 15);
            }
        } else {
            if (loserDetail.getElo() - winnerDetail.getElo() >= 50) {
                winnerDetail.setElo(winnerDetail.getElo() + 25);
                loserDetail.setElo(loserDetail.getElo() - 25);
            } else {
                winnerDetail.setElo(winnerDetail.getElo() + 15);
                loserDetail.setElo(loserDetail.getElo() - 15);
            }
        }
        userRepo.save(winnerDetail);
        userRepo.save(loserDetail);

        gameRepo.save(games.get(id));
        games.remove(id);
    }
}
