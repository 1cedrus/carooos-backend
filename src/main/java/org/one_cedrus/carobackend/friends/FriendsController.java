package org.one_cedrus.carobackend.friends;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.UserService;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendsController {

    private final FriendsService friendsService;

    @GetMapping
    public ResponseEntity<?> getFriends(Principal principal) {
        return ResponseEntity.ok(
            friendsService.getUserFriendData(principal.getName())
        );
    }

    @PostMapping("/{receiver}")
    public ResponseEntity<?> friendRequest(
        @PathVariable String receiver,
        Principal principal
    ) {
        friendsService.handleFriendRequest(principal.getName(), receiver);

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{receiver}")
    public ResponseEntity<?> friendsCancel(
        @PathVariable String receiver,
        Principal principal
    ) {
        friendsService.handleFriendCancel(principal.getName(), receiver);

        return ResponseEntity.accepted().build();
    }
}
