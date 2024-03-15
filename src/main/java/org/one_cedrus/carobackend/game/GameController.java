package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.game.dto.DrawMessage;
import org.one_cedrus.carobackend.game.dto.FinishMessage;
import org.one_cedrus.carobackend.game.dto.JoinMessage;
import org.one_cedrus.carobackend.game.dto.MoveMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@RestController
@RequiredArgsConstructor
public class GameController {
    private final SimpMessagingTemplate template;
    private final GameService gameService;
    private final TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> timeChecker = new HashMap<>();

    @MessageMapping("/game/{id}")
    public void process(
            @DestinationVariable String id,
            @Payload String payload,
            Principal principal
    ) {
        Game game = gameService.findPlayingGame(id).orElseThrow(() -> new RuntimeException("Game haven't init!"));

        Short move = Short.parseShort(payload);
        String moveUser = game.nextMoveUser();

        if (!moveUser.equals(principal.getName())) {
            throw new Error(moveUser + " " + principal.getName() + " is not a same person");
        }

        if (game.getMoves().contains(move)) {
            throw new RuntimeException("This spot already be marked!");
        }

        game.getMoves().add(move);
        template.convertAndSend("/topic/game/" + id,
                MoveMessage.builder().move(move).nextMove(game.nextMoveUser()).build());

        timeChecker.get(id).cancel(false);
        var checker = taskScheduler.schedule(() -> {
            game.setWinner(moveUser);
            template.convertAndSend("/topic/game/" + id, FinishMessage.builder().winner(moveUser).build());
            gameService.finishGame(id);

            timeChecker.remove(id);
        }, Instant.now().plus(60, ChronoUnit.SECONDS));
        timeChecker.put(id, checker);

        if (game.isFinish()) {
            template.convertAndSend("/topic/game/" + id, FinishMessage.builder().winner(moveUser).build());
            gameService.finishGame(id);

            timeChecker.get(id).cancel(false);
            timeChecker.remove(id);
        } else if (game.isDraw()) {
            template.convertAndSend("/topic/game/" + id, DrawMessage.builder().build());
            gameService.drawGame(id);
        }
    }

    @MessageMapping("/join/{id}")
    public void joinGame(
            @DestinationVariable String id,
            Principal principal
    ) {
        String sender = principal.getName();
        String firstUser = id.substring(0, id.indexOf("-"));
        String secondUser = id.substring(id.indexOf("-") + 1);

        if (!sender.equals(firstUser) && !sender.equals(secondUser)) {
            throw new RuntimeException(String.format("%s does not have authorization", sender));
        }

        if (gameService.findPlayingGame(id).isPresent()) {
            Game game = gameService.findPlayingGame(id).get();
            template.convertAndSend("/topic/game/" + id, JoinMessage.builder().currentMoves(game.getMoves()).nextMove(game.nextMoveUser()).build());
        } else {
            gameService.submitGame(sender, id);

            if (timeChecker.containsKey(id)) {
                return;
            }

            var initGame = taskScheduler.schedule(() -> {
                if (gameService.findGameByUsername(firstUser).equals(id) && gameService.findGameByUsername(secondUser).equals(id)) {
                    Game game = gameService.newGame(id);
                    template.convertAndSend("/topic/game/" + id, JoinMessage.builder().currentMoves(game.getMoves()).nextMove(game.getFirstMove()).build());

                    var checker = taskScheduler.schedule(() -> {
                        var winner = game.getFirstMove().equals(firstUser) ? secondUser : firstUser;
                        game.setWinner(winner);
                        var loser = game.getWinner().equals(firstUser) ? secondUser : firstUser;
                        game.setLoser(loser);
                        template.convertAndSend("/topic/game/" + id, FinishMessage.builder().winner(winner).build());
                        gameService.finishGame(id);

                        timeChecker.remove(id);
                    }, Instant.now().plus(60, ChronoUnit.SECONDS));
                    timeChecker.put(id, checker);
                } else {
                    gameService.removeGame(sender);
                    timeChecker.remove(id);
                }
            }, Instant.now().plus(5, ChronoUnit.SECONDS));

            timeChecker.put(id, initGame);
        }
    }


    @GetMapping("/api/game")
    public ResponseEntity<?> currentGame(Principal principal) {
        return ResponseEntity.ok().body(gameService.findGameByUsername(principal.getName()));
    }
}
