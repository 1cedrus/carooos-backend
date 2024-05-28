package org.one_cedrus.carobackend.user;

import java.security.Principal;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.dto.ChangeEmailRequest;
import org.one_cedrus.carobackend.user.dto.PubUserInfo;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInformation(Principal principal) {
        return ResponseEntity.ok(userService.getInfo(principal.getName()));
    }

    @PostMapping("/api/user/profile-picture")
    public ResponseEntity<?> updateProfilePicture(
        @RequestParam("image") MultipartFile file,
        Principal principal
    ) {
        userService.setProfilePic(principal.getName(), file);

        return ResponseEntity.ok(userService.getInfo(principal.getName()));
    }

    @PostMapping("/api/user/email")
    public ResponseEntity<?> updateEmail(
        @RequestBody ChangeEmailRequest request,
        Principal principal
    ) {
        userService.updateEmail(principal.getName(), request.getEmail());

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/api/public/user")
    public ResponseEntity<?> getPublicInformation(
        @RequestParam String username
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(Duration.ofSeconds(10)))
            .body(userService.getPubInfo(username));
    }

    @GetMapping("/api/public/users")
    public ResponseEntity<List<PubUserInfo>> listPublicUsersInformation(
        @RequestParam String query
    ) {
        return ResponseEntity.ok(userService.listPublicUsersInformation(query));
    }

    @GetMapping("/api/public/leader-board")
    public ResponseEntity<?> listPublicUsersInformation() {
        return ResponseEntity.ok(userService.getLeaderBoard());
    }
}
