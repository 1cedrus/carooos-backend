package org.one_cedrus.carobackend.friends;

import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.excepetion.BadFriendsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FriendsExceptionHandler {
    @ExceptionHandler({BadFriendsRequest.class})
    public ErrorDetails badFriendsRequest(BadFriendsRequest excep) {
        return ErrorDetails.builder().message(excep.getMessage()).build();
    }
}
