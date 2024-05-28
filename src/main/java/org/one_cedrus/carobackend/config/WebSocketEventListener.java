package org.one_cedrus.carobackend.config;

import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event)
        throws IOException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        var user = Objects.requireNonNull(accessor.getUser()).getName();

        StandardWebSocketSession session =
            (StandardWebSocketSession) Objects.requireNonNull(
                accessor.getSessionAttributes()
            ).get("SESSION");

        userService.setOnline(user, session);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(
        SessionDisconnectEvent event
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        StandardWebSocketSession session =
            (StandardWebSocketSession) Objects.requireNonNull(
                accessor.getSessionAttributes()
            ).get("SESSION");

        userService.setOffline(
            Objects.requireNonNull(accessor.getUser()).getName(),
            session
        );
    }

    // This one only for check if user has sub or unsub from a messages broker
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        var destination = headerAccessor.getDestination();
        var user = Objects.requireNonNull(headerAccessor.getUser()).getName();

        assert destination != null;
        if (destination.startsWith("/topic/messages/")) {
            redisTemplate.opsForValue().set(user + "messages", destination);
        }
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(
            event.getMessage()
        );

        var destination = Objects.requireNonNull(
            headerAccessor.getNativeHeader("id")
        ).getFirst();
        var user = Objects.requireNonNull(headerAccessor.getUser()).getName();

        assert destination != null;
        if (destination.startsWith("/topic/messages/")) {
            redisTemplate.opsForValue().set(user + "messages", "");
        }
    }
}
