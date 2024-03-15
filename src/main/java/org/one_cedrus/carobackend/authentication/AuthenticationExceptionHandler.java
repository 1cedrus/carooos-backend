package org.one_cedrus.carobackend.authentication;

import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.excepetion.BadRegisterRequest;
import org.one_cedrus.carobackend.excepetion.UsernameExistedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AuthenticationExceptionHandler {

    @ExceptionHandler({UsernameNotFoundException.class, AuthenticationException.class})
    public ErrorDetails usernameNotFoundException() {
        return ErrorDetails.builder().message("Username not found!").build();
    }

    @ExceptionHandler({UsernameExistedException.class})
    public ErrorDetails usernameExistedException() {
        return ErrorDetails.builder().message("Username existed!").build();
    }

    @ExceptionHandler({BadRegisterRequest.class})
    public ErrorDetails badRegisterRequest() {
        return ErrorDetails.builder().message("Bad register request!").build();
    }
}
