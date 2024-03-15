package org.one_cedrus.carobackend.authentication;


import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.authentication.dto.AuthenticateRequest;
import org.one_cedrus.carobackend.authentication.dto.RegisterRequest;
import org.one_cedrus.carobackend.excepetion.UsernameExisted;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            return ResponseEntity.ok(authenticationService.register(request));
        } catch (UsernameExisted _e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message("username is already existed!").build());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticateRequest request
    ) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorDetails.builder().message("username or password is wrong!").build());
        }
    }
}
