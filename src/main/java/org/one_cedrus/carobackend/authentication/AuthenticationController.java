package org.one_cedrus.carobackend.authentication;


import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationRequest;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody AuthenticationRequest request
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
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody String token
    ) {
        return ResponseEntity.ok(authenticationService.verify(token));
    }
}
