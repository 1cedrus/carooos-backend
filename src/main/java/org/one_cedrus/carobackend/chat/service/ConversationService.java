package org.one_cedrus.carobackend.chat.service;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.dto.ConversationInfo;
import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.model.Message;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.one_cedrus.carobackend.chat.repository.MessageRepository;
import org.one_cedrus.carobackend.chat.repository.UCRepository;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final SimpMessagingTemplate template;
    private final SimpUserRegistry userRegistry;
    private final UCRepository uCRepo;
    private final MessageRepository messageRepo;

    public ConversationInfo getInfo(Long uConversationId) {
        var uConversation = uCRepo.getReferenceById(uConversationId);
        var internalUCs = uCRepo.findUserConversationsByConversation(uConversation.getConversation());
        var peers = internalUCs.stream().map(o -> o.getUser().getUsername()).toList();
        var lastMessage = messageRepo.getFirstByConversationOrderByIdDesc(uConversation.getConversation());

        return ConversationInfo.builder().ucid(uConversationId).peers(peers).seen(uConversation.getSeen()).lastMessage(lastMessage).build();
    }

    public UserConversation ensureInConversation(User user, Long uConversationId) {
        var userConversations = user.getUserConversations();
        var uConversation = uCRepo.getReferenceById(uConversationId);

        if (!userConversations.stream().map(UserConversation::getId).toList().contains(uConversationId)) {
            throw new RuntimeException("Caller does not in this conversation");
        }

        return uConversation;
    }

    public void spreadMessage(Message message, Conversation conversation) {
        var internalUCs = uCRepo.findUserConversationsByConversation(conversation);
        internalUCs.forEach(o -> {
            var gonnaSendUser = o.getUser().getUsername();
            if (userRegistry.getUser(gonnaSendUser) != null) {
                template.convertAndSendToUser(gonnaSendUser, "/topic/messages", message);
            } else {
                o.setSeen(false);
                uCRepo.save(o);
            }
        });
    }
}
