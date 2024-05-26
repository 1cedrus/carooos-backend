package org.one_cedrus.carobackend.config;

import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event)
        throws IOException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        var user = accessor.getUser();

        WebSocketSession session = (WebSocketSession) Objects.requireNonNull(
            accessor.getSessionAttributes()
        ).get("SESSION");

        if (user == null) {
            session.close();
        } else {
            userService.setOnline(user.getName(), session);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(
        SessionDisconnectEvent event
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        userService.setOffline(
            Objects.requireNonNull(accessor.getUser()).getName()
        );
    }
}
