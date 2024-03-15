package org.one_cedrus.carobackend.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.one_cedrus.carobackend.user.dto.UserMessageType;

@Data
@Builder
@AllArgsConstructor
public class UserMessage {
    private UserMessageType type;
    private String username;
}
