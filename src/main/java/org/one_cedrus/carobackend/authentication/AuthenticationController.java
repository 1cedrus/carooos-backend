package org.one_cedrus.carobackend.authentication;


import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.authentication.dto.AuthenticateRequest;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationResponse;
import org.one_cedrus.carobackend.authentication.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticateRequest request
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
