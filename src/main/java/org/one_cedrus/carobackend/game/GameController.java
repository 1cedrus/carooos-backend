package org.one_cedrus.carobackend.game;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class GameController {
    private final SimpMessagingTemplate template;
    private final GameService gameService;

    @MessageMapping("/game/{id}")
    public void process(
            @DestinationVariable String id,
            @Payload String payload,
            Principal principal
    ) {
        Game game = gameService.findPlayingGame(id).orElseThrow(() -> new Error("Game haven't init!"));

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

        if (game.isFinish()) {
            template.convertAndSend("/topic/game/" + id, FinishMessage.builder().winner(moveUser).build());
            gameService.finishGame(id);
        }
    }

    @MessageMapping("/join/{id}")
    public void joinGame(
            @DestinationVariable String id,
            Principal principal
    ) {
        String firstUser = id.substring(0, id.indexOf("-"));
        String secondUser = id.substring(id.indexOf("-") + 1);

        var mayBeGame = gameService.findPlayingGame(id);
        Game game;
        if (firstUser.endsWith(principal.getName()) || secondUser.endsWith(principal.getName())) {
            game = mayBeGame.orElseGet(() -> gameService.newGame(id));
        } else {
            game = mayBeGame.orElseThrow(() -> new RuntimeException("Game does not existed!"));
        }

        String nextMove = game.getMoves().size() % 2 == 0 ? game.getFirstMove() : game.getFirstMove().equals(firstUser) ? secondUser : firstUser;

        template.convertAndSend("/topic/game/" + id, JoinMessage.builder().currentMoves(game.getMoves()).nextMove(nextMove).build());
    }
}
