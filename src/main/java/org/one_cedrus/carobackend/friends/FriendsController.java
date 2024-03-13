package org.one_cedrus.carobackend.friends;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendsController {
    private final UserRepository userRepo;
    private final SimpMessagingTemplate template;

    @PostMapping("/{username}")
    public ResponseEntity<?> friendRequest(
            @PathVariable String username,
            Principal principal
    ) {
        try {
            User sender = userRepo.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));
            User receiver = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

            if (sender.getUsername().equals(receiver.getUsername())) {
                throw new RuntimeException("Sender and receiver is same person");
            }

            // Receiver already sent rq to be friend with sender
            if (sender.getRequests().contains(receiver.getUsername())) {
                sender.getRequests().remove(receiver.getUsername());

                sender.getFriends().add(receiver.getUsername());
                receiver.getFriends().add(sender.getUsername());

                userRepo.save(sender);
                userRepo.save(receiver);

                template.convertAndSendToUser(receiver.getUsername(), "/topic/friends", FriendsMessage.builder().type(FriendsMessageType.FriendResponse).username(sender.getUsername()).build());
            } else if (!receiver.getRequests().contains(sender.getUsername())) {
                receiver.getRequests().add(principal.getName());
                userRepo.save(receiver);

                template.convertAndSendToUser(receiver.getUsername(), "/topic/friends", FriendsMessage.builder().type(FriendsMessageType.FriendRequest).username(sender.getUsername()).build());
            }

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> friendsCancel(
            @PathVariable String username,
            Principal principal
    ) {
        try {
            User sender = userRepo.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));

            if (sender.getRequests().contains(username)) {
                sender.getRequests().remove(username);

                userRepo.save(sender);
            } else if (sender.getFriends().contains(username)) {
                var target = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

                sender.getFriends().remove(username);
                target.getFriends().remove(sender.getUsername());

                userRepo.save(sender);
                userRepo.save(target);
            } else {
                throw new RuntimeException(String.format("%s and %s does not in any friendship situation", sender.getUsername(), username));
            }

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()).build());
        }
    }

    @PostMapping("invite/{username}")
    public ResponseEntity<?> inviteForMatch(
            @PathVariable String username,
            Principal principal
    ) {
        try {
            var receiver = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

            if (receiver.getFriends().contains(principal.getName())) {
                template.convertAndSendToUser(username, "/topic/friends", FriendsMessage.builder().type(FriendsMessageType.InviteRequest).username(principal.getName()).build());
            }

            return ResponseEntity.accepted().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()).build());
        }
    }


    @PostMapping("response/{username}")
    public ResponseEntity<?> responseInvite(
            @PathVariable String username,
            Principal principal
    ) {
        try {
            var receiver = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

            if (receiver.getFriends().contains(principal.getName())) {
                template.convertAndSendToUser(username, "/topic/friends", FriendsMessage.builder().type(FriendsMessageType.InviteResponse).username(principal.getName()).build());
            }

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()).build());
        }
    }
}
