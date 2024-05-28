package org.one_cedrus.carobackend.game.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoveMessage {

    private final GameMessageType type = GameMessageType.Move;
    private Short move;
    private String nextMove;
    private LocalDateTime lastMoveTimeStamp;
}
