package org.one_cedrus.carobackend.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticateRequest {
    private String username;
    private String password;
}
