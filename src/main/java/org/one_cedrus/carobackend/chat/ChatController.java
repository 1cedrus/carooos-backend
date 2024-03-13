package org.one_cedrus.carobackend.chat;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class ChatController {
    private final SimpMessagingTemplate template;
    private final UserRepository userRepo;
    private final ChatRepository chatRepo;

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody ChatMessage message
    ) {
        try {
            var sender = userRepo.findByUsername(message.getSender()).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", message.getSender())));

            if (!sender.getFriends().contains(message.getReceiver()))
                throw new RuntimeException("Sender and receiver are not in friendship");

            message.setTimeStamp(LocalDateTime.now());
            chatRepo.save(message);

            template.convertAndSendToUser(message.getReceiver(), "/topic/messages", message);

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()));
        }
    }

    @GetMapping
    private ResponseEntity<?> listMessage(Principal principal) {
        try {
            var sender = userRepo.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));

            var sentMessages = chatRepo.getChatMessagesBySenderOrderByTimeStampDesc(sender.getUsername(), PageRequest.of(0, 10));
            var receivedMessages = chatRepo.getChatMessagesByReceiverOrderByTimeStampDesc(sender.getUsername(), PageRequest.of(0, 10));

            sentMessages.addAll(receivedMessages);

            return ResponseEntity.ok(sentMessages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()));
        }
    }
}
