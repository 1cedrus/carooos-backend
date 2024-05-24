package org.one_cedrus.carobackend.auth;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.auth.dto.AuthenticationRequest;
import org.one_cedrus.carobackend.auth.dto.AuthenticationResponse;
import org.one_cedrus.carobackend.auth.dto.ChangePasswordRequest;
import org.one_cedrus.carobackend.auth.dto.RegisterRequest;
import org.one_cedrus.carobackend.user.UserRepository;
import org.one_cedrus.carobackend.user.UserService;
import org.one_cedrus.carobackend.user.model.Role;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    //TODO: Rewrite it, just for development term
    private Integer generateResetToken() {
        return new Random().nextInt(999999 - 100000) + 100000;
    }

    public void changePasswordWithToken(
        String email,
        String token,
        String password
    ) {
        verifyResetToken(email, token);

        var user = userService.getUser(email);

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    public void verifyResetToken(String email, String submittedToken) {
        var user = userService.getUser(email);

        var maybeToken = redisTemplate.opsForValue().get(user.getUsername());

        if (maybeToken == null || !maybeToken.equals(submittedToken)) {
            throw new RuntimeException("Invalid token");
        }
    }

    //TODO: Maybe we need transaction here
    public void resetPassword(String email) {
        var user = userService.getUser(email);

        if (redisTemplate.opsForValue().get(user.getUsername()) != null) {
            throw new RuntimeException("Wait for 5 minutes or try later");
        }

        var token = generateResetToken().toString();
        emailService.sendEmail(
            user.getEmail(),
            "Reset Password Token",
            String.format(
                "Your reset password token is %s.\n If you do not request this, please ignore this email!",
                token
            )
        );
        redisTemplate
            .opsForValue()
            .setIfAbsent(user.getUsername(), token, 5, TimeUnit.MINUTES);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        var user = userService.getUser(username);
        var oldPassword = request.getOldPassword();
        var newPassword = request.getNewPassword();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("The old password is not match");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException(
                "New password should be different with old one"
            );
        }

        ensureValidPassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public AuthenticationResponse verify(String token) {
        final String username = jwtService.extractUsername(token);

        var user = userService.getUser(username);
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Access token is not valid");
        }

        return AuthenticationResponse.builder().token(token).build();
    }

    public AuthenticationResponse register(RegisterRequest request) {
        ensureRegisterInfo(request);

        var email = request.getEmail();
        var username = request.getUsername();
        var password = request.getPassword();

        var user = User.builder()
            .email(email)
            .username(username)
            .password(passwordEncoder.encode(password))
            .role(Role.ROLE_USER)
            .elo(0)
            .build();
        userRepo.save(user);

        return AuthenticationResponse.builder()
            .token(jwtService.generateToken(user))
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userService.getUser(request.getUsernameOrEmail());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                request.getPassword()
            )
        );

        return AuthenticationResponse.builder()
            .token(jwtService.generateToken(user))
            .build();
    }

    private void ensureRegisterInfo(RegisterRequest request) {
        ensureValidEmail(request.getEmail());
        ensureValidUsername(request.getUsername());
        ensureValidPassword(request.getPassword());
    }

    private void ensureValidEmail(String email) {
        if (userService.isExisted(email)) {
            throw new RuntimeException("Submitted email is existed");
        } else if (
            !email.matches("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        ) {
            throw new RuntimeException("Email is not valid");
        }
    }

    private void ensureValidUsername(String username) {
        if (userService.isExisted(username)) {
            throw new RuntimeException("Submitted username is existed");
        } else if (
            username.length() <= 3 ||
            username.length() > 16 ||
            !username.matches("^[a-zA-Z0-9_]+$")
        ) {
            throw new RuntimeException("Submitted username is not valid");
        }
    }

    private void ensureValidPassword(String password) {
        if (password.length() < 5 || password.length() > 32) {
            throw new RuntimeException(
                "Password must be between 5 and 32 characters"
            );
        }
    }
}
