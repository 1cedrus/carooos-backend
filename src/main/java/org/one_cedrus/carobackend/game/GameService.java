package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.game.dto.DrawMessage;
import org.one_cedrus.carobackend.game.dto.FinishMessage;
import org.one_cedrus.carobackend.game.dto.JoinMessage;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

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

    public boolean endOrDraw(String roomCode) {
        Game game = games.get(roomCode);

        if (game.isFinish()) {
            template.convertAndSend("/topic/game/" + roomCode, FinishMessage.builder().winner(game.getWinner()).build());
            finishGame(roomCode);
            return true;
        } else if (game.isDraw()) {
            template.convertAndSend("/topic/game/" + roomCode, DrawMessage.builder().build());
            drawGame(roomCode);
            return true;
        }

        return false;
    }

    public ScheduledFuture<?> initGame(String roomCode) {
        if (onInitGame.containsKey(roomCode)) {
            return onInitGame.get(roomCode);
        }

        String firstUser = Game.roomCodeToFirstUser(roomCode);
        String secondUser = Game.roomCodeToSecondUser(roomCode);

        var initGame = taskScheduler.schedule(() -> {
            boolean isBothUsersJoined = findGameByUsername(firstUser).equals(roomCode) && findGameByUsername(secondUser).equals(roomCode);

            if (isBothUsersJoined) {
                Game game = newGame(roomCode);

                template.convertAndSend("/topic/game/" + roomCode,
                        JoinMessage.builder()
                                .currentMoves(game.getMoves())
                                .nextMove(game.getFirstMoveUser())
                                .build()
                );

                endGameIfMoveUserNotMove(game, true);
            } else {
                unSubmitGame(firstUser, roomCode);
                unSubmitGame(secondUser, roomCode);
            }

            onInitGame.remove(roomCode);
        }, Instant.now().plus(5, ChronoUnit.SECONDS));

        onInitGame.put(roomCode, initGame);
        return initGame;
    }

    public ScheduledFuture<?> endGameIfMoveUserNotMove(Game game, boolean _firstMove) {
        return taskScheduler.schedule(() -> {
            if (game.getMoves().isEmpty()) {
                template.convertAndSend("/topic/game/" + game.getRoomCode(),
                        FinishMessage.builder().winner(game.nextMoveUser()).build());

                finishGame(game.getRoomCode());
            }
        }, Instant.now().plus(60, ChronoUnit.SECONDS));
    }

    public ScheduledFuture<?> endGameIfMoveUserNotMove(Game game) {
        return taskScheduler.schedule(() -> {
            template.convertAndSend("/topic/game/" + game.getRoomCode(),
                    FinishMessage.builder().winner(game.nextMoveUser()).build());

            finishGame(game.getRoomCode());
        }, Instant.now().plus(60, ChronoUnit.SECONDS));
    }


    public void submitGame(String username, String id) {
        usernameToGame.put(username, id);
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

    private void drawGame(String roomCode) {
        Game game = games.get(roomCode);

        usernameToGame.remove(game.firstUser());
        usernameToGame.remove(game.secondUser());

        gameRepo.save(game);
        games.remove(roomCode);
    }

    private void finishGame(String roomCode) {
        Game game = games.get(roomCode);

        unSubmitGame(game.firstUser(), roomCode);
        unSubmitGame(game.secondUser(), roomCode);

        var winner = userRepo.findByUsername(game.getWinner()).orElseThrow();
        var loser = userRepo.findByUsername(game.getLoser()).orElseThrow();

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

        userRepo.save(winner);
        userRepo.save(loser);

        gameRepo.save(game);
        games.remove(roomCode);
    }

    private Game newGame(String roomCode) {
        Game game = Game.newGame(roomCode);

        games.put(roomCode, game);
        return game;
    }
}
