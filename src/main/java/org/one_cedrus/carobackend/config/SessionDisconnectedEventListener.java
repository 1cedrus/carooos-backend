package org.one_cedrus.carobackend.config;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class SessionDisconnectedEventListener
    implements ApplicationListener<SessionDisconnectEvent> {

    private final UserService userService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String username = Objects.requireNonNull(event.getUser()).getName();
        userService.setOffline(username);
    }
}
