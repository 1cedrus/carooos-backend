package org.one_cedrus.carobackend.user;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.ErrorDetails;
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
        try {
            User sender = userRepo.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(String.format("Error occurred when trying to fetch %s information", principal.getName())));
            return ResponseEntity.ok(sender.getUserInformation());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()));
        }
    }

    @GetMapping("/api/public/user")
    public ResponseEntity<?> getPublicInformation(
            @RequestParam String username
    ) {
        try {
            User user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("Username does not existed: %s ", username)));
            return ResponseEntity.ok(user.getPublicUserInformation());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message(e.getMessage()));
        }
    }

    @GetMapping("/api/public/users")
    public ResponseEntity<List<PublicUserInformation>> listPublicUsersInformation(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(userService.listPublicUsersInformation(query));
    }
}
