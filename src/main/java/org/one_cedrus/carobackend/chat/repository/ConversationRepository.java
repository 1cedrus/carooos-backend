package org.one_cedrus.carobackend.chat.repository;

import org.one_cedrus.carobackend.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository
    extends JpaRepository<Conversation, Long> {}
