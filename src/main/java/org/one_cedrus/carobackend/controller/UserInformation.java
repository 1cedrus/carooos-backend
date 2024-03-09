package org.one_cedrus.carobackend.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.one_cedrus.carobackend.user.Role;

@Data
@Builder
@AllArgsConstructor
public class UserInformation {
    private String username;
    private Integer elo;
    private Role role;
}
