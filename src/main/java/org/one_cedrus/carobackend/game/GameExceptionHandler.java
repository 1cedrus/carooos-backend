package org.one_cedrus.carobackend.game;

import org.one_cedrus.carobackend.ErrorDetails;
import org.one_cedrus.carobackend.excepetion.GameException;
import org.one_cedrus.carobackend.excepetion.GameNotFound;
import org.one_cedrus.carobackend.excepetion.NotHasPermit;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class GameExceptionHandler {
    @ExceptionHandler({GameNotFound.class})
    public ErrorDetails gameNotFound() {
        return ErrorDetails.builder().message("Game haven't been initialized yet!").build();
    }

    @ExceptionHandler({NotHasPermit.class})
    public ErrorDetails notHasPermit() {
        return ErrorDetails.builder().message("You don't have permit to do this action!").build();
    }

    @ExceptionHandler({GameException.class})
    public ErrorDetails gameException(GameException excep) {
        return ErrorDetails.builder().message(excep.getMessage()).build();
    }
}

