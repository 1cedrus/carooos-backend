package org.one_cedrus.carobackend.excepetion;

public class BadFriendsRequest extends RuntimeException {
    public BadFriendsRequest(String msg) {
        super(msg);
    }
}
