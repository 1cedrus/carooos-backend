package org.one_cedrus.carobackend.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String passwordConfirmation;
}
