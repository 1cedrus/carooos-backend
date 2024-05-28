package org.one_cedrus.carobackend.auth.service;

import jakarta.mail.MessagingException;
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
        var maybeToken = redisTemplate.opsForValue().get(email);

        if (maybeToken == null || !maybeToken.equals(submittedToken)) {
            throw new RuntimeException("Invalid token");
        }
    }

    //TODO: Maybe we need transaction here
    public void resetPassword(String email) throws MessagingException {
        var user = userService.getUser(email);

        if (redisTemplate.opsForValue().get(email) != null) {
            throw new RuntimeException("Wait for 5 minutes or try later");
        }

        var token = generateResetToken().toString();
        emailService.sendEmail(
            email,
            "Reset Password Token",
            String.format(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            border: 1px solid #ddd;
                            border-radius: 10px;
                        }
                        .header {
                            background-color: #f8f8f8;
                            padding: 10px 20px;
                            border-bottom: 1px solid #ddd;
                            text-align: center;
                            font-size: 24px;
                            font-weight: bold;
                        }
                        .content {
                            padding: 20px;
                        }
                        .token-box {
                            background-color: #f8f8f8;
                            padding: 15px;
                            margin: 20px 0;
                            border: 1px solid #ddd;
                            border-radius: 5px;
                            text-align: center;
                            font-size: 18px;
                            font-weight: bold;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                        }
                        .footer a {
                            color: #1a73e8;
                            text-decoration: none;
                        }
                        .footer a:hover {
                            text-decoration: underline;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            CAROOOS! Password Reset Request
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>We received a request to reset your password for your CAROOOS! account associated with this email: %s.</p>
                            <div class="token-box">
                                Your reset password token is:<br><strong>%s</strong>
                            </div>
                            <p>This token is only valid for 5 minutes.</p>
                            <p>If you did not request a password reset, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>If you have any questions or need further assistance, feel free to <a href="#">contact our support team</a>.</p>
                            <p>Best regards,<br>The CAROOOS! Team</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                user.getUsername(),
                email,
                token
            )
        );

        redisTemplate
            .opsForValue()
            .setIfAbsent(email, token, 5, TimeUnit.MINUTES);
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
