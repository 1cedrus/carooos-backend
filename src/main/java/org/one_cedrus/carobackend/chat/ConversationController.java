package org.one_cedrus.carobackend.chat;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.dto.RawMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/{conversationId}")
    public ResponseEntity<?> sendMessage(
        Principal principal,
        @RequestBody RawMessage rawMessage,
        @PathVariable Long conversationId
    ) {
        conversationService.sendMessage(
            principal.getName(),
            conversationId,
            rawMessage
        );

        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<?> getConversations(
        Principal principal,
        @RequestParam(defaultValue = "0") Integer from,
        @RequestParam(defaultValue = "10") Integer perPage,
        @RequestParam(defaultValue = "") String peerQuery
    ) {
        return ResponseEntity.ok(
            conversationService.getInfoInRange(
                principal.getName(),
                from,
                perPage,
                peerQuery
            )
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> listConversationMessages(
        Principal principal,
        @PathVariable Long conversationId,
        @RequestParam(required = false) Long nonce,
        @RequestParam(defaultValue = "0") Integer from,
        @RequestParam(defaultValue = "10") Integer perPage
    ) {
        return ResponseEntity.ok(
            conversationService.listConversationMessages(
                principal.getName(),
                nonce,
                conversationId,
                from,
                perPage
            )
        );
    }
}
