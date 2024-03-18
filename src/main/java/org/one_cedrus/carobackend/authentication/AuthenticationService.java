package org.one_cedrus.carobackend.authentication;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.authentication.dto.AuthenticateRequest;
import org.one_cedrus.carobackend.authentication.dto.AuthenticationResponse;
import org.one_cedrus.carobackend.authentication.dto.RegisterRequest;
import org.one_cedrus.carobackend.excepetion.BadRegisterRequest;
import org.one_cedrus.carobackend.excepetion.UsernameExistedException;
import org.one_cedrus.carobackend.user.Role;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthenticationResponse register(RegisterRequest request) {
        var username = request.getUsername();
        var password = request.getPassword();

        if (username.length() <= 3
                || username.length() > 16
                || password.length() < 5
                || password.length() > 32
                || !username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BadRegisterRequest();
        }

        if (userRepo.findByUsername(username).isPresent()) {
            throw new UsernameExistedException();
        }

        var user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .friends(new ArrayList<>())
                .requests(new ArrayList<>())
                .role(Role.ROLE_USER)
                .elo(0)
                .build();
        userRepo.save(user);

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticateRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userRepo.findByUsername(request.getUsername()).orElseThrow();

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }
}
