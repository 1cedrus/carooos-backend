package org.one_cedrus.carobackend.user;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.dto.PublicUserInformation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepo;

    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInformation(Principal principal) {
        User sender = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", principal.getName())));
        return ResponseEntity.ok(sender.getUserInformation());
    }

    @GetMapping("/api/public/user")
    public ResponseEntity<?> getPublicInformation(
            @RequestParam String username
    ) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("%s not found", username)));
        return ResponseEntity.ok(user.getPublicUserInformation());
    }

    @GetMapping("/api/public/users")
    public ResponseEntity<List<PublicUserInformation>> listPublicUsersInformation(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(userService.listPublicUsersInformation(query));
    }
}
