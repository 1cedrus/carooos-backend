package org.one_cedrus.carobackend.excepetion;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GameException extends RuntimeException {
    public GameException(String msg) {
        super(msg);
    }
}
