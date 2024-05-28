package org.one_cedrus.carobackend.game;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.game.dto.DrawMessage;
import org.one_cedrus.carobackend.game.dto.FinishMessage;
import org.one_cedrus.carobackend.game.dto.JoinMessage;
import org.one_cedrus.carobackend.user.UserRepository;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate template;
    private final Map<String, Game> games = new HashMap<>();
    private final Map<String, String> usernameToGame = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> onInitGame = new HashMap<>();
    private final Map<String, LocalDateTime> gameToLastMove = new HashMap<>();
    private final RouterFunctionMapping routerFunctionMapping;

    public void setLastMove(String roomCode, LocalDateTime timeStamp) {
        gameToLastMove.put(roomCode, timeStamp);
    }

    public LocalDateTime getLastMove(String roomCode) {
        return gameToLastMove.get(roomCode);
    }

    public boolean endOrDraw(String roomCode) {
        Game game = games.get(roomCode);

        if (game.isFinish()) {
            finishGame(roomCode);
            template.convertAndSend(
                "/topic/game/" + roomCode,
                FinishMessage.builder().winner(game.getWinner()).build()
            );
            return true;
        } else if (game.isDraw()) {
            drawGame(roomCode);
            template.convertAndSend(
                "/topic/game/" + roomCode,
                DrawMessage.builder().build()
            );
            return true;
        }

        return false;
    }

    public void initGame(String roomCode) {
        String firstUser = Game.roomCodeToFirstUser(roomCode);
        String secondUser = Game.roomCodeToSecondUser(roomCode);

        boolean isBothUsersJoined =
            findGameByUsername(firstUser).equals(roomCode) &&
            findGameByUsername(secondUser).equals(roomCode);

        if (isBothUsersJoined) {
            Game game = newGame(roomCode);

            template.convertAndSend(
                "/topic/game/" + roomCode,
                JoinMessage.builder()
                    .currentMoves(game.getMoves())
                    .lastMoveTimeStamp(gameToLastMove.get(roomCode))
                    .nextMove(game.getFirstMoveUser())
                    .build()
            );

            endGameIfMoveUserNotMove(game, true);
        }

        if (onInitGame.containsKey(roomCode)) {
            return;
        }

        var initGame = taskScheduler.schedule(
            () -> {
                if (!games.containsKey(roomCode)) {
                    unSubmitGame(firstUser, roomCode);
                    unSubmitGame(secondUser, roomCode);

                    onInitGame.remove(roomCode);
                }
            },
            Instant.now().plus(5, ChronoUnit.SECONDS)
        );

        onInitGame.put(roomCode, initGame);
    }

    public void endGameIfMoveUserNotMove(Game game, boolean _firstMove) {
        taskScheduler.schedule(
            () -> {
                if (game.getMoves().isEmpty()) {
                    game.setWinner(game.nextMoveUser());
                    finishGame(game.getRoomCode());

                    template.convertAndSend(
                        "/topic/game/" + game.getRoomCode(),
                        FinishMessage.builder()
                            .winner(game.nextMoveUser())
                            .build()
                    );
                }
            },
            Instant.now().plus(60, ChronoUnit.SECONDS)
        );
    }

    public ScheduledFuture<?> endGameIfMoveUserNotMove(Game game) {
        return taskScheduler.schedule(
            () -> {
                game.setWinner(game.nextMoveUser());
                finishGame(game.getRoomCode());

                template.convertAndSend(
                    "/topic/game/" + game.getRoomCode(),
                    FinishMessage.builder().winner(game.nextMoveUser()).build()
                );
            },
            Instant.now().plus(60, ChronoUnit.SECONDS)
        );
    }

    public void submitGame(String username, String roomCode) {
        usernameToGame.put(username, roomCode);
    }

    public void unSubmitGame(String username, String roomCode) {
        if (findGameByUsername(username).equals(roomCode)) {
            usernameToGame.remove(username);
        }
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

    private User getUser(String username) {
        return userRepo
            .findByUsername(username)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        String.format("%s does not existed", username)
                    )
            );
    }

    private void drawGame(String roomCode) {
        Game game = games.get(roomCode);

        unSubmitGame(game.firstUser(), roomCode);
        unSubmitGame(game.secondUser(), roomCode);
        gameToLastMove.remove(roomCode);

        var firstUser = getUser(game.firstUser());
        var secondUser = getUser(game.secondUser());

        firstUser.getGames().add(game);
        secondUser.getGames().add(game);

        userRepo.saveAll(List.of(firstUser, secondUser));
        gameRepo.save(game);
        games.remove(roomCode);
    }

    private void finishGame(String roomCode) {
        Game game = games.get(roomCode);

        unSubmitGame(game.firstUser(), roomCode);
        unSubmitGame(game.secondUser(), roomCode);
        gameToLastMove.remove(roomCode);

        var winner = getUser(game.getWinner());
        var loser = getUser(game.getLoser());

        if (winner.getElo() > loser.getElo()) {
            if (winner.getElo() - loser.getElo() >= 50) {
                winner.setElo(winner.getElo() + 5);
                loser.setElo(loser.getElo() - 5);
            } else {
                winner.setElo(winner.getElo() + 15);
                loser.setElo(loser.getElo() - 15);
            }
        } else {
            if (loser.getElo() - winner.getElo() >= 50) {
                winner.setElo(winner.getElo() + 25);
                loser.setElo(loser.getElo() - 25);
            } else {
                winner.setElo(winner.getElo() + 15);
                loser.setElo(loser.getElo() - 15);
            }
        }

        winner.getGames().add(game);
        loser.getGames().add(game);

        userRepo.saveAll(List.of(winner, loser));
        gameRepo.save(game);
        games.remove(roomCode);
    }

    private Game newGame(String roomCode) {
        Game game = Game.newGame(roomCode);

        games.put(roomCode, game);
        gameToLastMove.put(roomCode, LocalDateTime.now());
        return game;
    }
}
