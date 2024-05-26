package org.one_cedrus.carobackend.chat.model;

import jakarta.persistence.*;
import lombok.*;
import org.one_cedrus.carobackend.user.model.User;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserConversation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Conversation conversation;

    private Integer numberOfUnseen;

    public static UserConversation create(
        User user,
        Conversation conversation
    ) {
        return UserConversation.builder()
            .user(user)
            .conversation(conversation)
            .numberOfUnseen(0)
            .build();
    }
}
