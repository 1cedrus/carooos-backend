package org.one_cedrus.carobackend.friends;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.one_cedrus.carobackend.user.model.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendsController {
    private final UserService userService;
    private final FriendsService friendsService;

    @PostMapping("/{username}")
    public ResponseEntity<?> friendRequest(
        @PathVariable String username,
        Principal principal
    ) {
        User sender = userService.getUser(principal.getName());
        User receiver = userService.getUser(username);

        boolean isSamePerson = sender.getUsername().equals(receiver.getUsername());
        if (isSamePerson) {
            throw new RuntimeException("Caller and receiver is same person");
        }

        friendsService.handleFriendRequest(sender, receiver);

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> friendsCancel(
        @PathVariable String username,
        Principal principal
    ) {
        User sender = userService.getUser(principal.getName());
        User receiver = userService.getUser(username);

        friendsService.handleFriendCancel(sender, receiver);

        return ResponseEntity.accepted().build();
    }
}
