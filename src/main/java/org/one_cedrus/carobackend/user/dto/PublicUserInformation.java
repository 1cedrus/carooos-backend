package org.one_cedrus.carobackend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PublicUserInformation {
    private String username;
    private Integer elo;
}
