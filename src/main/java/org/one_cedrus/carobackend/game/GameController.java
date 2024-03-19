package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.excepetion.GameException;
import org.one_cedrus.carobackend.excepetion.GameNotFound;
import org.one_cedrus.carobackend.excepetion.NotHasPermit;
import org.one_cedrus.carobackend.game.dto.CurrentGame;
import org.one_cedrus.carobackend.game.dto.JoinMessage;
import org.one_cedrus.carobackend.game.dto.MoveMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@RestController
@RequiredArgsConstructor
public class GameController {
    private final SimpMessagingTemplate template;
    private final GameService gameService;
    private final Map<String, ScheduledFuture<?>> timeChecker = new HashMap<>();

    @MessageMapping("/game/{roomCode}")
    public void process(
            @DestinationVariable String roomCode,
            @Payload String payload,
            Principal principal
    ) {
        String sender = principal.getName();
        Game game = gameService.findPlayingGame(roomCode).orElseThrow(GameNotFound::new);

        Short move = Short.parseShort(payload);
        String moveUser = game.moveUser();
        String nextMoveUser = game.nextMoveUser();

        if (!moveUser.equals(sender)) {
            throw new NotHasPermit();
        }

        if (game.getMoves().contains(move)) {
            throw new GameException("This spot already be marked!");
        }

        game.getMoves().add(move);
        template.convertAndSend("/topic/game/" + roomCode,
                MoveMessage.builder()
                        .move(move)
                        .nextMove(nextMoveUser)
                        .build());

        if (timeChecker.containsKey(roomCode)) {
            timeChecker.get(roomCode).cancel(false);
        }

        if (gameService.endOrDraw(roomCode)) {
            timeChecker.remove(roomCode);
        } else {
            timeChecker.put(roomCode, gameService.endGameIfMoveUserNotMove(game));
        }
    }

    @MessageMapping("/join/{roomCode}")
    public void joinGame(
            @DestinationVariable String roomCode,
            Principal principal
    ) {
        String sender = principal.getName();
        String firstUser = Game.roomCodeToFirstUser(roomCode);
        String secondUser = Game.roomCodeToSecondUser(roomCode);

        if (!sender.equals(firstUser) && !sender.equals(secondUser)) {
            throw new GameException(String.format("%s does not have authorization", sender));
        }

        if (gameService.findPlayingGame(roomCode).isPresent()) {
            Game game = gameService.findPlayingGame(roomCode).get();
            template.convertAndSend("/topic/game/" + roomCode, JoinMessage.builder()
                    .currentMoves(game.getMoves())
                    .nextMove(game.nextMoveUser())
                    .build()
            );
        } else {
            gameService.submitGame(sender, roomCode);
            gameService.initGame(roomCode);
        }
    }


    @GetMapping("/api/game")
    public ResponseEntity<?> currentGame(Principal principal) {
        return ResponseEntity.ok().body(CurrentGame.builder().game(gameService.findGameByUsername(principal.getName())).build());
    }
}
