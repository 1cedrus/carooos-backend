package org.one_cedrus.carobackend.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinMessage {
    private final GameMessageType type = GameMessageType.Join;
    private List<Short> currentMoves;
    private String nextMove;
}
