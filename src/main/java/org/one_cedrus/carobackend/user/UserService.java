package org.one_cedrus.carobackend.user;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.service.ConversationService;
import org.one_cedrus.carobackend.game.Game;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.one_cedrus.carobackend.user.dto.UserInfo;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ConversationService cService;
    private final UserRepository userRepo;

    public boolean isExisted(String username) {
        return userRepo.existsById(username);
    }

    public UserInfo getInfo(String username) {
        var user = getUser(username);

        return UserInfo
            .builder()
            .username(username)
            .elo(user.getElo())
            .friends(user.getFriends())
            .requests(user.getRequests())
            .games(user.getGames().stream().map(Game::getId).toList())
            .conversations(user.getUserConversations().stream().map(o -> cService.getInfo(o.getId())).toList())
            .build();
    }

    public PubUserInfo getPubInfo(String username) {
        var user = getUser(username);

        return PubUserInfo
            .builder()
            .username(username)
            .elo(user.getElo())
            .build();
    }

    public User getUser(String username) {
        return userRepo.findByUsername(username).orElseThrow(
            () -> new UsernameNotFoundException(String.format("%s does not existed", username))
        );
    }

    public List<PubUserInfo> listPublicUsersInformation(String query) {
        return userRepo
            .findByUsernameStartingWith(query, PageRequest.of(0, 10))
            .stream()
            .map(user -> PubUserInfo
                .builder()
                .username(user.getUsername())
                .elo(user.getElo())
                .build())
            .toList();
    }
}
