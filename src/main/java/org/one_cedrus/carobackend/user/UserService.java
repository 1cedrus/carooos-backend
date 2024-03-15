package org.one_cedrus.carobackend.user;

import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.dto.PublicUserInformation;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;

    public List<PublicUserInformation> listPublicUsersInformation(String query) {
        return userRepo
                .findByUsernameStartingWith(query, PageRequest.of(0, 10))
                .stream()
                .map(user -> PublicUserInformation
                        .builder()
                        .username(user.getUsername())
                        .elo(user.getElo())
                        .build())
                .toList();
    }
}
