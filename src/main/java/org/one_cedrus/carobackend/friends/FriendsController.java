package org.one_cedrus.carobackend.friends;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.excepetion.BadFriendsRequest;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendsController {
    private final UserRepository userRepo;
    private final FriendsService friendsService;

    @PostMapping("/{username}")
    public ResponseEntity<?> friendRequest(
            @PathVariable String username,
            Principal principal
    ) {
        User sender = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));
        User receiver = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

        if (sender.getUsername().equals(receiver.getUsername())) {
            throw new BadFriendsRequest("Same person!");
        }

        friendsService.handleFriendRequest(sender, receiver);

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> friendsCancel(
            @PathVariable String username,
            Principal principal
    ) {
        User sender = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));
        User target = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));

        friendsService.handleFriendCancel(sender, target);

        return ResponseEntity.accepted().build();
    }
}
