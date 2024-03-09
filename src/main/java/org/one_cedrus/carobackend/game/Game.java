package org.one_cedrus.carobackend.game;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;

import java.util.List;
import java.util.stream.IntStream;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    @Id
    private String id;
    @Transient
    private List<Short> moves;
    private String firstMove;
    private String winner;
    private String loser;


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
            setWinner(lastMoveIndex % 2 == 0 ? firstMove : firstMove.equals(firstUser()) ? secondUser() : firstUser());
            setLoser(getWinner().equals(firstUser()) ? secondUser() : firstUser());
        }

        return isFinish;
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

    public String nextMoveUser() {
        return moves.size() % 2 == 0 ? firstMove : (firstMove.equals(firstUser()) ? secondUser() : firstUser());
    }

    public String firstUser() {
        return id.substring(0, id.indexOf("-"));
    }

    public String secondUser() {
        return id.substring(id.indexOf("-") + 1);
    }
}

