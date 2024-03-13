package org.one_cedrus.carobackend.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserInformation {
    private String username;
    private Integer elo;
    private Role role;
    private List<String> friends;
    private List<String> requests;
}
