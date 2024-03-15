package org.one_cedrus.carobackend.game.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinishMessage {
    private final GameMessageType type = GameMessageType.Finish;
    private String winner;
}
