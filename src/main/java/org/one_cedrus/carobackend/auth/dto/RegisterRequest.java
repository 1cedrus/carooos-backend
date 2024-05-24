package org.one_cedrus.carobackend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    private String email;
    private String username;
    private String password;
}
