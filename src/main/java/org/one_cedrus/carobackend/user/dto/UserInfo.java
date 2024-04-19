package org.one_cedrus.carobackend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.one_cedrus.carobackend.chat.dto.ConversationInfo;
import org.one_cedrus.carobackend.user.model.Role;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserInfo {
    private String username;
    private Integer elo;
    private Role role;
    private String currentGame;
    private List<String> friends;
    private List<String> requests;
    private List<ConversationInfo> conversations;
}
