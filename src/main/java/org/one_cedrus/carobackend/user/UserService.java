package org.one_cedrus.carobackend.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.ConversationService;
import org.one_cedrus.carobackend.game.GameService;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.one_cedrus.carobackend.user.dto.UserInfo;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ConversationService cService;
    private final GameService gameService;
    private final UserRepository userRepo;

    public boolean isExisted(String usernameOrEmail) {
        return userRepo.existsByUsernameOrEmail(
            usernameOrEmail,
            usernameOrEmail
        );
    }

    public UserInfo getInfo(String username) {
        var user = getUser(username);
        var currentGame = gameService.findGameByUsername(username);

        return UserInfo.builder()
            .username(username)
            .elo(user.getElo())
            .friends(user.getFriends())
            .requests(user.getRequests())
            .conversations(
                user
                    .getUserConversations()
                    .stream()
                    .map(o -> cService.getInfo(o.getId()))
                    .toList()
            )
            .currentGame(currentGame)
            .build();
    }

    public PubUserInfo getPubInfo(String username) {
        var user = getUser(username);

        return PubUserInfo.builder()
            .username(username)
            .elo(user.getElo())
            .build();
    }

    public User getUser(String usernameOrEmail) {
        return userRepo
            .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        String.format("%s does not existed", usernameOrEmail)
                    )
            );
    }

    public List<PubUserInfo> listPublicUsersInformation(String query) {
        return userRepo
            .findByUsernameStartingWith(query, PageRequest.of(0, 10))
            .stream()
            .map(
                user ->
                    PubUserInfo.builder()
                        .username(user.getUsername())
                        .elo(user.getElo())
                        .build()
            )
            .toList();
    }
}
