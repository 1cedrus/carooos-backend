package org.one_cedrus.carobackend.auth;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.auth.dto.*;
import org.one_cedrus.carobackend.auth.service.AuthenticationService;
import org.one_cedrus.carobackend.game.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final GameRepository gameRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthenticationResponse> verify(
        @RequestBody String token
    ) {
        return ResponseEntity.ok(authenticationService.verify(token));
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthenticationResponse> changePassword(
        @RequestBody ChangePasswordRequest request,
        Principal principal
    ) {
        authenticationService.changePassword(principal.getName(), request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationResponse> resetPassword(
        @RequestBody ResetPasswordRequest request
    ) {
        authenticationService.resetPassword(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password/verify")
    public ResponseEntity<AuthenticationResponse> verifyToken(
        @RequestBody ResetPasswordRequest request
    ) {
        authenticationService.verifyResetToken(
            request.getEmail(),
            request.getToken()
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password/complete")
    public ResponseEntity<AuthenticationResponse> completeReset(
        @RequestBody ResetPasswordRequest request
    ) {
        authenticationService.changePasswordWithToken(
            request.getEmail(),
            request.getToken(),
            request.getNewPassword()
        );
        return ResponseEntity.accepted().build();
    }
}
