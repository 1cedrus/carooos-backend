package org.one_cedrus.carobackend.controller;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.game.Game;
import org.one_cedrus.carobackend.game.GameService;
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

        game.getMoves().add(move);
        template.convertAndSend("/topic/game/" + id,
                MoveMessage.builder().move(move).nextMove(game.nextMoveUser()).build().toString());

        if (game.isFinish()) {
            template.convertAndSend("/topic/game/" + id, FinishMessage.builder().winner(moveUser).build().toString());
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

        if (firstUser.endsWith(principal.getName()) || secondUser.endsWith(principal.getName())) {
            Game game = gameService.findPlayingGame(id).orElseGet(() -> gameService.newGame(id));
            String nextMove = game.getMoves().size() % 2 == 0 ? game.getFirstMove() : game.getFirstMove().equals(firstUser) ? secondUser : firstUser;

            template.convertAndSend("/topic/game/" + id, JoinMessage.builder().currentMoves(game.getMoves()).nextMove(nextMove).build().toString());
        }
    }
}
