package org.one_cedrus.carobackend.chat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.dto.ConversationInfo;
import org.one_cedrus.carobackend.chat.dto.Pagination;
import org.one_cedrus.carobackend.chat.dto.RawMessage;
import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.model.Message;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.one_cedrus.carobackend.chat.repository.ConversationRepository;
import org.one_cedrus.carobackend.chat.repository.MessageRepository;
import org.one_cedrus.carobackend.chat.repository.UCRepository;
import org.one_cedrus.carobackend.user.UserService;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final SimpMessagingTemplate template;
    private final SimpUserRegistry userRegistry;
    private final UCRepository uCRepo;
    private final MessageRepository messageRepo;
    private final UserService userService;
    private final ConversationRepository conversationRepo;
    private final RedisTemplate<String, Integer> redisTemplate;

    public Pagination<ConversationInfo> getInfoInRange(
        String username,
        Integer from,
        Integer perPage,
        String peerQuery
    ) {
        var user = userService.getUser(username);

        var topUserConversation =
            uCRepo.findUserConversationsByUserAndConversation_UserConversations_User_UsernameStartingWithOrderByConversation_LastMessage_TimeStampDesc(
                user,
                peerQuery,
                PageRequest.of(from, perPage)
            );

        var conversationsInfo = topUserConversation
            .stream()
            .map(o -> getInfo(o.getId()))
            .toList();

        return Pagination.<ConversationInfo>builder()
            .from(from)
            .perPage(perPage)
            .total(user.getUserConversations().size())
            .hasNextPage(
                (from + 1) * perPage < user.getUserConversations().size()
            )
            .items(conversationsInfo)
            .build();
    }

    public ConversationInfo getInfo(Long uConversationId) {
        var uConversation = uCRepo.getReferenceById(uConversationId);
        var internalUCs = uCRepo.findUserConversationsByConversation(
            uConversation.getConversation()
        );

        var peers = internalUCs
            .stream()
            .map(o -> o.getUser().getUsername())
            .toList();

        return ConversationInfo.builder()
            .cid(uConversation.getConversation().getId())
            .peers(peers)
            .numberOfUnseen(uConversation.getNumberOfUnseen())
            .lastMessage(uConversation.getConversation().getLastMessage())
            .build();
    }

    public UserConversation ensureInConversation(
        User user,
        Long conversationId
    ) {
        return uCRepo
            .getUserConversationByUserAndConversation_Id(user, conversationId)
            .orElseThrow(
                () ->
                    new RuntimeException("Caller does not in this conversation")
            );
    }

    public void sendMessage(
        String username,
        Long conversationId,
        RawMessage rawMessage
    ) {
        var user = userService.getUser(username);
        var uConversation = ensureInConversation(user, conversationId);

        var conversation = uConversation.getConversation();
        var newMessage = Message.create(
            username,
            uConversation.getConversation(),
            rawMessage.getContent()
        );

        conversation.setNumOfMessages(conversation.getNumOfMessages() + 1);
        conversationRepo.save(conversation);

        messageRepo.save(newMessage);
        spreadMessage(newMessage, conversation);
    }

    private void spreadMessage(Message message, Conversation conversation) {
        var internalUCs = uCRepo.findUserConversationsByConversation(
            conversation
        );

        internalUCs.forEach(o -> {
            var gonnaSendUser = o.getUser().getUsername();

            if (userRegistry.getUser(gonnaSendUser) != null) {
                var userSession = (SimpSession) Arrays.stream(
                    Objects.requireNonNull(userRegistry.getUser(gonnaSendUser))
                        .getSessions()
                        .toArray()
                )
                    .findFirst()
                    .orElseThrow();

                var mayBeSubscription = userSession
                    .getSubscriptions()
                    .stream()
                    .filter(
                        s ->
                            s
                                .getDestination()
                                .equals(
                                    "/topic/messages/" + conversation.getId()
                                )
                    )
                    .findFirst();

                if (mayBeSubscription.isEmpty()) {
                    o.setNumberOfUnseen(o.getNumberOfUnseen() + 1);
                }

                template.convertAndSendToUser(
                    gonnaSendUser,
                    "/topic/messages",
                    message
                );
            }
        });

        uCRepo.saveAll(internalUCs);
    }

    public Pagination<Message> listConversationMessages(
        String username,
        Long nonce,
        Long conversationId,
        Integer from,
        Integer perPage
    ) {
        var timeStamp = LocalDateTime.now();
        if (nonce != null) {
            timeStamp = Timestamp.from(
                Instant.ofEpochMilli(nonce)
            ).toLocalDateTime();
        }

        var user = userService.getUser(username);
        var uConversation = ensureInConversation(user, conversationId);
        var conversation = uConversation.getConversation();

        var numOfMessages = conversation.getNumOfMessages();

        var messages =
            messageRepo.findChatMessagesByConversationAndTimeStampBeforeOrderByTimeStampDesc(
                conversation,
                timeStamp,
                PageRequest.of(from, perPage)
            );

        if (nonce != null) {
            numOfMessages = redisTemplate
                .opsForValue()
                .get(conversationId + "_" + nonce);

            if (numOfMessages == null) {
                numOfMessages =
                    messageRepo.countChatMessagesByConversationAndTimeStampBefore(
                        conversation,
                        timeStamp
                    );

                redisTemplate
                    .opsForValue()
                    .set(
                        conversationId + "_" + nonce,
                        numOfMessages,
                        5,
                        TimeUnit.MINUTES
                    );
            }
        }

        uConversation.setNumberOfUnseen(0);
        uCRepo.save(uConversation);

        return Pagination.<Message>builder()
            .items(messages)
            .from(from)
            .perPage(perPage)
            .hasNextPage((from + 1) * perPage < numOfMessages)
            .total(numOfMessages)
            .build();
    }
}
