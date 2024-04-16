package org.one_cedrus.carobackend.errors;

import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ErrorsHandler {
    @ExceptionHandler({RuntimeException.class, AuthenticationException.class})
    public ErrorDetails handleRuntimeExp(Exception excep) {
        return ErrorDetails.builder()
            .status(400)
            .timestamp(LocalDateTime.now())
            .detail(excep.getMessage()).build();
    }
}
