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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ConversationService cService;
    private final GameService gameService;
    private final ImageService imageService;
    private final UserRepository userRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void updateEmail(String username, String email) {
        ensureValidEmail(email);
        var user = getUser(username);

        user.setEmail(email);
        userRepo.save(user);
    }

    public void setProfilePic(String username, MultipartFile file) {
        var user = getUser(username);
        var picName = imageService.uploadImage(file);

        user.setImageUrl(picName);
        userRepo.save(user);
    }

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
            .email(user.getEmail())
            .profilePicUrl(user.getImageUrl())
            .build();
    }

    public PubUserInfo getPubInfo(String username) {
        var user = getUser(username);

        return PubUserInfo.builder()
            .username(username)
            .elo(user.getElo())
            .imageUrl(user.getImageUrl())
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
                        .imageUrl(user.getImageUrl())
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

    private void ensureValidEmail(String email) {
        if (isExisted(email)) {
            throw new RuntimeException("Submitted email is existed");
        } else if (
            !email.matches("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        ) {
            throw new RuntimeException("Email is not valid");
        }
    }
}
