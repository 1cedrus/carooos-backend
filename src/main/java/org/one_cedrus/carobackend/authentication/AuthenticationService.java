package org.one_cedrus.carobackend.authentication;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationRequest;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationResponse;
import org.one_cedrus.carobackend.user.UserService;
import org.one_cedrus.carobackend.user.model.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthenticationResponse verify(String token) {
        final String username = jwtService.extractUsername(token);

        var user = userService.getUser(username);
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Access token is not valid");
        }

        return AuthenticationResponse.builder().token(token).build();
    }

    public AuthenticationResponse register(AuthenticationRequest request) {
        ensureRegisterInfo(request);

        var username = request.getUsername();
        var password = request.getPassword();
        var user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .elo(0)
            .build();
        userRepo.save(user);

        return AuthenticationResponse.builder()
            .token(jwtService.generateToken(user))
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userService.getUser(request.getUsername());

        return AuthenticationResponse.builder()
            .token(jwtService.generateToken(user))
            .build();
    }

    private void ensureRegisterInfo(AuthenticationRequest request) {
        var username = request.getUsername();
        var password = request.getPassword();

        if (username.length() <= 3
            || username.length() > 16
            || password.length() < 5
            || password.length() > 32
            || !username.matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("Submitted information is not valid");
        }

        if (userService.isExisted(username)) {
            throw new RuntimeException("Submitted username is existed");
        }
    }
}
