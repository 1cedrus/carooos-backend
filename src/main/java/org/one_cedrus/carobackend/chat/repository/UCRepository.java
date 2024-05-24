package org.one_cedrus.carobackend.chat.repository;

import java.util.List;
import java.util.Optional;
import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.one_cedrus.carobackend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UCRepository extends JpaRepository<UserConversation, Long> {
    List<UserConversation> findUserConversationsByConversation(
        Conversation conver
    );

    Optional<UserConversation> getUserConversationByUserAndConversation_Id(
        User user,
        Long conversationId
    );
}
