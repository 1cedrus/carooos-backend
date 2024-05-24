package org.one_cedrus.carobackend.user;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInformation(Principal principal) {
        return ResponseEntity.ok(userService.getInfo(principal.getName()));
    }

    @GetMapping("/api/public/user")
    public ResponseEntity<?> getPublicInformation(
        @RequestParam String username
    ) {
        return ResponseEntity.ok(userService.getPubInfo(username));
    }

    @GetMapping("/api/public/users")
    public ResponseEntity<List<PubUserInfo>> listPublicUsersInformation(
        @RequestParam String query
    ) {
        return ResponseEntity.ok(userService.listPublicUsersInformation(query));
    }
}
