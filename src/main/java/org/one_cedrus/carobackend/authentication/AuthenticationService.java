package org.one_cedrus.carobackend.authentication;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.controller.AuthenticateRequest;
import org.one_cedrus.carobackend.controller.RegisterRequest;
import org.one_cedrus.carobackend.user.Role;
import org.one_cedrus.carobackend.user.User;
import org.one_cedrus.carobackend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    public AuthenticationResponse register(RegisterRequest request) {
        try {
            userDetailsService.loadUserByUsername(request.getUsername());
        } catch (UsernameNotFoundException _e) {
            var user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.ROLE_USER)
                    .elo(0)
                    .build();

            userRepo.save(user);
            return AuthenticationResponse.builder()
                    .token(jwtService.generateToken(user))
                    .build();
        }

        return AuthenticationResponse.builder().error("username is already existed!").build();
    }

    public AuthenticationResponse authenticate(AuthenticateRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            var user = userRepo.findByUsername(request.getUsername()).orElseThrow();
            return AuthenticationResponse.builder()
                    .token(jwtService.generateToken(user))
                    .build();
        } catch (Exception _e) {
            return AuthenticationResponse.builder().error("username or password is wrong!!").build();
        }
    }
}
