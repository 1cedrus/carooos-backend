package org.one_cedrus.carobackend.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.ConversationService;
import org.one_cedrus.carobackend.friends.dto.FriendsMessage;
import org.one_cedrus.carobackend.friends.dto.FriendsMessageType;
import org.one_cedrus.carobackend.game.GameService;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.one_cedrus.carobackend.user.dto.UserInfo;
import org.one_cedrus.carobackend.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(
        UserService.class
    );
    private final ConversationService cService;
    private final GameService gameService;
    private final UserRepository userRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

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

    public void setOnline(String username) {
        var user = getUser(username);

        redisTemplate.opsForSet().add("onlineTracking", username);

        user
            .getFriends()
            .forEach(o -> {
                simpMessagingTemplate.convertAndSendToUser(
                    o,
                    "/topic/online",
                    FriendsMessage.builder()
                        .username(username)
                        .type(FriendsMessageType.FriendOnline)
                        .build()
                );
            });
    }

    public void setOffline(String username) {
        var user = getUser(username);

        redisTemplate.opsForSet().remove("onlineTracking", username);

        user
            .getFriends()
            .forEach(
                o ->
                    simpMessagingTemplate.convertAndSendToUser(
                        o,
                        "/topic/online",
                        FriendsMessage.builder()
                            .username(username)
                            .type(FriendsMessageType.FriendOffline)
                            .build()
                    )
            );
    }
}
