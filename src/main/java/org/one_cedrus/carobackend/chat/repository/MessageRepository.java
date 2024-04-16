package org.one_cedrus.carobackend.chat.repository;

import org.one_cedrus.carobackend.chat.model.Conversation;
import org.one_cedrus.carobackend.chat.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> getChatMessagesByConversationOrderByTimeStampDesc(Conversation conversation, Pageable pageable);

    Message getFirstByConversationOrderByIdDesc(Conversation conversation);
}
