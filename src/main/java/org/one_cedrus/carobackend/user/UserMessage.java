package org.one_cedrus.carobackend.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserMessage {
    private UserMessageType type;
    private String username;
}
