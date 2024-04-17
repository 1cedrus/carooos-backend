package org.one_cedrus.carobackend.chat;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.dto.Pagination;
import org.one_cedrus.carobackend.chat.dto.RawMessage;
import org.one_cedrus.carobackend.chat.model.Message;
import org.one_cedrus.carobackend.chat.repository.MessageRepository;
import org.one_cedrus.carobackend.chat.repository.UCRepository;
import org.one_cedrus.carobackend.chat.service.ConversationService;
import org.one_cedrus.carobackend.user.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class ChatController {
    private final UserService userService;
    private final ConversationService cService;
    private final MessageRepository messageRepo;
    private final UCRepository uCRepo;

    @PostMapping
    public ResponseEntity<?> sendMessage(
        @RequestBody RawMessage rawMessage
    ) {
        var sender = userService.getUser(rawMessage.getSender());
        var uConversation = cService.ensureInConversation(sender, rawMessage.getCid());
        var conversation = uConversation.getConversation();
        var newMessage = Message.create(sender.getUsername(), uConversation.getConversation(), rawMessage.getContent());

        messageRepo.save(newMessage);
        cService.spreadMessage(newMessage, conversation);

        return ResponseEntity.accepted().build();
    }


    @GetMapping("/{conversationId}")
    private ResponseEntity<?> listConversationMessages(
        @PathVariable String conversationId,
        @RequestParam(defaultValue = "0") String from,
        @RequestParam(defaultValue = "10") String perPage,
        Principal principal
    ) {
        var sender = userService.getUser(principal.getName());
        var uConversation = cService.ensureInConversation(sender, Long.parseLong(conversationId));
        var conversation = uConversation.getConversation();

        var fromInt = Integer.parseInt(from);
        var perPageInt = Integer.parseInt(perPage);
        var numOfMessages = conversation.getNumOfMessages();
        var messages = messageRepo.getChatMessagesByConversationOrderByTimeStampDesc(conversation, PageRequest.of(fromInt, perPageInt));

        uConversation.setSeen(true);
        uCRepo.save(uConversation);

        return ResponseEntity.ok(
            Pagination.<Message>builder()
                .items(messages)
                .from(fromInt)
                .perPage(perPageInt)
                .hasNextPage((fromInt + 1) * perPageInt < numOfMessages)
                .total(numOfMessages)
                .build()
        );
    }
}
