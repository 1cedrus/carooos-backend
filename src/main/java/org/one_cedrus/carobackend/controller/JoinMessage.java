package org.one_cedrus.carobackend.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinMessage {
    private final GameMessageType type = GameMessageType.Join;
    private List<Short> currentMoves;
    private String nextMove;

    @Override
    public String toString() {
        return String.format("{\"type\": \"%s\", \"currentMoves\": %s, \"nextMove\": \"%s\"}", type, currentMoves, nextMove);
    }
}
