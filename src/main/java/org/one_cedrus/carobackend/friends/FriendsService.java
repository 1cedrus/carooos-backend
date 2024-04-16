package org.one_cedrus.carobackend.friends;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.repository.ConversationRepository;
import org.one_cedrus.carobackend.chat.repository.UCRepository;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.one_cedrus.carobackend.friends.dto.FriendsMessage;
import org.one_cedrus.carobackend.friends.dto.FriendsMessageType;
import org.one_cedrus.carobackend.user.model.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendsService {
    private final UserRepository userRepo;
    private final UCRepository conversationRepository;
    private final ConversationRepository chatConversationRepository;
    private final SimpMessagingTemplate template;

    public void ensureNotInFriendship(User sender, User receiver) {
        if (sender.getFriends().contains(receiver.getUsername())) {
            throw new RuntimeException("Caller and receiver is already in friendship!");
        }
    }

    public void handleFriendRequest(User sender, User receiver) {
        ensureNotInFriendship(sender, receiver);

        var isReceiverRequested = sender.getRequests().contains(receiver.getUsername());
        if (isReceiverRequested) {
            sender.getRequests().remove(receiver.getUsername());

            sender.getFriends().add(receiver.getUsername());
            receiver.getFriends().add(sender.getUsername());

            var conversation = Conversation.builder().numOfMessages(0).build();
            chatConversationRepository.save(conversation);
            userRepo.saveAll(List.of(sender, receiver));

            var uCSender = UserConversation.create(sender, conversation);
            var uCReceiver = UserConversation.create(receiver, conversation);
            conversationRepository.saveAll(List.of(uCSender, uCReceiver));

            template.convertAndSendToUser(
                receiver.getUsername(),
                "/topic/friends",
                FriendsMessage
                    .builder()
                    .type(FriendsMessageType.FriendResponse)
                    .username(sender.getUsername())
                    .build());
        } else if (!receiver.getRequests().contains(sender.getUsername())) {
            receiver.getRequests().add(sender.getUsername());
            userRepo.save(receiver);

            template.convertAndSendToUser(
                receiver.getUsername(),
                "/topic/friends",
                FriendsMessage
                    .builder()
                    .type(FriendsMessageType.FriendRequest)
                    .username(sender.getUsername())
                    .build());
        }
    }

    public void handleFriendCancel(User sender, User target) {
        if (sender.getRequests().contains(target.getUsername())) {
            sender.getRequests().remove(target.getUsername());

            userRepo.save(sender);
        } else if (sender.getFriends().contains(target.getUsername())) {
            sender.getFriends().remove(target.getUsername());
            target.getFriends().remove(sender.getUsername());

            userRepo.save(sender);
            userRepo.save(target);
        } else {
            throw new RuntimeException(String.format("%s and %s does not in any friendship situation", sender.getUsername(), target.getUsername()));
        }
    }
}
