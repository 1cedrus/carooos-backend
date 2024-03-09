package org.one_cedrus.carobackend.controller;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<UserInformation> getUserInformation() {
        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        User userDetails = (User) userDetailsService.loadUserByUsername(username);

        return ResponseEntity.ok(UserInformation.builder()
                .username(userDetails.getUsername())
                .elo(userDetails.getElo())
                .role(userDetails.getRole())
                .build());
    }

    @GetMapping("public")
    public ResponseEntity<UserInformation> getSpecificUserInformation(
            @RequestParam String username
    ) {
        User userDetails = (User) userDetailsService.loadUserByUsername(username);

        return ResponseEntity.ok(UserInformation.builder()
                .username(userDetails.getUsername())
                .elo(userDetails.getElo())
                .role(userDetails.getRole())
                .build());
    }
}
