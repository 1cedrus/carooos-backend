package org.one_cedrus.carobackend.chat.repository;

import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UCRepository extends JpaRepository<UserConversation, Long> {
    List<UserConversation> findUserConversationsByConversation(Conversation conver);
}
