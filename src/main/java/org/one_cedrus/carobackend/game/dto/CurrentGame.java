package org.one_cedrus.carobackend.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CurrentGame {
    private String game;
}
