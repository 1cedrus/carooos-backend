package org.one_cedrus.carobackend.friends;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.excepetion.BadFriendsRequest;
import org.one_cedrus.carobackend.friends.dto.FriendsMessage;
import org.one_cedrus.carobackend.friends.dto.FriendsMessageType;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendsService {
    private final UserRepository userRepo;
    private final SimpMessagingTemplate template;

    public void handleFriendRequest(User sender, User receiver) {
        if (sender.getFriends().contains(receiver.getUsername())) {
            throw new BadFriendsRequest("Already in friendship!");
        }

        if (sender.getRequests().contains(receiver.getUsername())) {
            sender.getRequests().remove(receiver.getUsername());

            sender.getFriends().add(receiver.getUsername());
            receiver.getFriends().add(sender.getUsername());

            userRepo.save(sender);
            userRepo.save(receiver);

            template.convertAndSendToUser(
                    receiver.getUsername(),
                    "/topic/friends",
                    FriendsMessage
                            .builder()
                            .type(FriendsMessageType.FriendResponse)
                            .username(sender.getUsername())
                            .build());
        }
        // Sender not in rq of receiver
        else if (!receiver.getRequests().contains(sender.getUsername())) {
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
            throw new BadFriendsRequest(String.format("%s and %s does not in any friendship situation", sender.getUsername(), target.getUsername()));
        }
    }
}
