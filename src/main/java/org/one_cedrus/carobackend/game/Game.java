package org.one_cedrus.carobackend.game;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private Long id;

    // roomCode in pattern `${firstUser}-${secondUser}`
    private String roomCode;

    @Convert(converter = MovesToStringConverter.class)
    private List<Short> moves;
    private String firstMoveUser;
    private String winner;

    private String remainingUser(String user) {
        return user.equals(firstUser()) ? secondUser() : firstUser();
    }

    static String randomFirstMove(String roomCode) {
        return new Random().nextInt(2) % 2 == 0
                ? roomCodeToFirstUser(roomCode)
                : roomCodeToSecondUser(roomCode);
    }

    static Game newGame(String roomCode) {
        return Game.builder()
                .roomCode(roomCode)
                .moves(new ArrayList<>())
                .firstMoveUser(randomFirstMove(roomCode))
                .build();
    }

    static String roomCodeToFirstUser(String roomCode) {
        return roomCode.substring(0, roomCode.indexOf("-"));
    }

    static String roomCodeToSecondUser(String roomCode) {
        return roomCode.substring(roomCode.indexOf("-") + 1);
    }

    public boolean isFinish() {
        int lastMoveIndex = moves.size() - 1;
        List<Short> movesOfPlayer = IntStream.range(0, lastMoveIndex).filter(i -> i % 2 == lastMoveIndex % 2).mapToObj(i -> moves.get(i)).toList();

        boolean isFinish =
                // Vertical
                calculate(movesOfPlayer, (short) 20)
                        // Horizontal
                        || calculate(movesOfPlayer, (short) 1)
                        // From top left to bottom right
                        || calculate(movesOfPlayer, (short) 21)
                        // From top right to bottom left
                        || calculate(movesOfPlayer, (short) 19);

        if (isFinish) {
            setWinner(nextMoveUser());
        }

        return isFinish;
    }

    public boolean isDraw() {
        return moves.size() == 400;
    }

    private boolean calculate(List<Short> movesOfPlayer, short operand) {
        int length = 1;
        short lastMove = moves.getLast();

        short tmp = lastMove;
        boolean isBound = false;
        while (length < 5) {
            if (movesOfPlayer.contains((short) (tmp + operand))) {
                length += 1;
                tmp += operand;
            } else if (!isBound) {
                operand = (short) -operand;
                tmp = lastMove;
                isBound = true;
            } else {
                break;
            }
        }

        return length >= 5;
    }

    public String moveUser() {
        return moves.size() % 2 == 0 ? firstMoveUser : remainingUser(firstMoveUser);
    }

    public String nextMoveUser() {
        return remainingUser(moveUser());
    }

    public String firstUser() {
        return roomCodeToFirstUser(roomCode);
    }

    public String secondUser() {
        return roomCodeToSecondUser(roomCode);
    }

    public String getLoser() {
        return winner != null ? remainingUser(winner) : null;
    }

}

