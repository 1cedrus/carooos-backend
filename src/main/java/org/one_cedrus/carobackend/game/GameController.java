package org.one_cedrus.carobackend.game;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.dto.Pagination;
import org.one_cedrus.carobackend.game.dto.JoinMessage;
import org.one_cedrus.carobackend.game.dto.MoveMessage;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final SimpMessagingTemplate template;
    private final GameService gameService;
    private final GameRepository gameRepository;
    private final UserService userService;

    private final Map<String, ScheduledFuture<?>> timeChecker = new HashMap<>();

    @MessageMapping("/game/{roomCode}")
    public void process(
        @DestinationVariable String roomCode,
        @Payload String payload,
        Principal principal
    ) {
        String caller = principal.getName();
        Game game = gameService
            .findPlayingGame(roomCode)
            .orElseThrow(() -> new RuntimeException("Game is not existed"));

        Short move = Short.parseShort(payload);
        String moveUser = game.moveUser();
        String nextMoveUser = game.nextMoveUser();

        if (!moveUser.equals(caller)) {
            throw new RuntimeException("Caller is not have permission");
        }

        if (game.getMoves().contains(move)) {
            throw new RuntimeException("This spot already be marked!");
        }

        game.getMoves().add(move);
        gameService.setLastMove(roomCode, LocalDateTime.now());
        template.convertAndSend(
            "/topic/game/" + roomCode,
            MoveMessage.builder()
                .move(move)
                .nextMove(nextMoveUser)
                .lastMoveTimeStamp(gameService.getLastMove(roomCode))
                .build()
        );

        if (timeChecker.containsKey(roomCode)) {
            timeChecker.get(roomCode).cancel(false);
        }

        if (gameService.endOrDraw(roomCode)) {
            timeChecker.remove(roomCode);
        } else {
            timeChecker.put(
                roomCode,
                gameService.endGameIfMoveUserNotMove(game)
            );
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
            throw new RuntimeException(
                String.format("%s does not have authorization", sender)
            );
        }

        if (gameService.findPlayingGame(roomCode).isPresent()) {
            Game game = gameService.findPlayingGame(roomCode).get();
            template.convertAndSend(
                "/topic/game/" + roomCode,
                JoinMessage.builder()
                    .currentMoves(game.getMoves())
                    .lastMoveTimeStamp(gameService.getLastMove(roomCode))
                    .nextMove(game.moveUser())
                    .build()
            );
        } else {
            gameService.submitGame(sender, roomCode);
            gameService.initGame(roomCode);
        }
    }

    @GetMapping("/api/game")
    public ResponseEntity<?> listGames(
        Principal principal,
        @RequestParam(defaultValue = "0") Integer from,
        @RequestParam(defaultValue = "10") Integer perPage
    ) {
        var caller = userService.getUser(principal.getName());
        var gamesOfCaller = gameRepository.countGamesByUsersContains(caller);

        var games = gameRepository.findGamesByUsersContainsOrderByIdDesc(
            caller,
            PageRequest.of(from, perPage)
        );

        return ResponseEntity.ok(
            Pagination.<Game>builder()
                .from(from)
                .perPage(perPage)
                .items(games)
                .hasNextPage((from + 1) * perPage < gamesOfCaller)
                .total(gamesOfCaller)
                .build()
        );
    }
}
