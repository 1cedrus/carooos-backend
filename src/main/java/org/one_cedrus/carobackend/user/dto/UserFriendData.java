package org.one_cedrus.carobackend.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFriendData {

    private List<FriendInformation> friends;
    private List<String> requests;
}
