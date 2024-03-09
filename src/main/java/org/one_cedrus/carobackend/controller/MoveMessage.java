package org.one_cedrus.carobackend.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoveMessage {
    private final GameMessageType type = GameMessageType.Move;
    private Short move;
    private String nextMove;

    @Override
    public String toString() {
        return String.format("{\"type\": \"%s\", \"move\": %d, \"nextMove\": \"%s\"}", type, move, nextMove);
    }
}
