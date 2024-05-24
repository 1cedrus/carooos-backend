package org.one_cedrus.carobackend.auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;
    private String newPassword;
    private String token;
}
