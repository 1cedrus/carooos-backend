package org.one_cedrus.carobackend.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessagesMessage {
    private final GameMessageType type = GameMessageType.Messages;
    private String sender;
    private String content;
}
