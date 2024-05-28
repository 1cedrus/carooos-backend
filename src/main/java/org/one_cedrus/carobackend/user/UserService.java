package org.one_cedrus.carobackend.user;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.friends.dto.FriendsMessage;
import org.one_cedrus.carobackend.friends.dto.FriendsMessageType;
import org.one_cedrus.carobackend.game.GameService;
import org.one_cedrus.carobackend.user.dto.FriendInformation;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.one_cedrus.carobackend.user.dto.UserInfo;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GameService gameService;
    private final ImageService imageService;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final HashMap<String, WebSocketSession> currentUserSessions =
        new HashMap<>();
    private final TaskScheduler taskScheduler;

    public List<PubUserInfo> getLeaderBoard() {
        return userRepo
            .findAllByOrderByEloDesc(PageRequest.of(0, 10))
            .stream()
            .map(o -> getPubInfo(o.getUsername()))
            .toList();
    }

    public List<FriendInformation> getFriendsInfo(String username) {
        var user = getUser(username);

        return user
            .getFriends()
            .stream()
            .map(
                o ->
                    FriendInformation.builder()
                        .username(o)
                        .isOnline(currentUserSessions.containsKey(o))
                        .build()
            )
            .toList();
    }

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
            .profilePicUrl(user.getImageUrl())
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
                        .profilePicUrl(user.getImageUrl())
                        .build()
            )
            .toList();
    }

    public void setOnline(String username, StandardWebSocketSession session) {
        var user = getUser(username);

        // Disconnect last sessionId
        var currentSession = currentUserSessions.get(username);
        currentUserSessions.put(username, session);

        if (currentSession != null) {
            simpMessagingTemplate.convertAndSendToUser(
                username,
                "/topic/online",
                "SOMEONE GOT INTO YOUR ACCOUNT, RUN AWAY!!!"
            );

            taskScheduler.schedule(
                () -> {
                    try {
                        currentSession.close();
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                },
                Instant.now().plus(500, ChronoUnit.MILLIS)
            );
        }

        currentUserSessions.put(username, session);

        if (currentSession == null) {
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
    }

    public void setOffline(String username, WebSocketSession session) {
        var user = getUser(username);

        if (currentUserSessions.get(username).equals(session)) {
            currentUserSessions.remove(username);
        }

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
