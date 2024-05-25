package org.one_cedrus.carobackend.config;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
@RequiredArgsConstructor
public class SessionConnectedEventListener
    implements ApplicationListener<SessionConnectedEvent> {

    private final UserService userService;

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        String username = Objects.requireNonNull(event.getUser()).getName();
        userService.setOnline(username);
    }
}
