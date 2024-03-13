package org.one_cedrus.carobackend.friends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FriendsMessage {
    private FriendsMessageType type;
    private String username;
}
