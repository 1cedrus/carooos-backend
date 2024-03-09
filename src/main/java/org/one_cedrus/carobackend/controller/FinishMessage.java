package org.one_cedrus.carobackend.controller;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinishMessage {
    private final GameMessageType type = GameMessageType.Finish;
    private String winner;

    @Override
    public String toString() {
        return String.format("{\"type\": \"%s\", \"winner\": \"%s\"}", type, winner);
    }
}
