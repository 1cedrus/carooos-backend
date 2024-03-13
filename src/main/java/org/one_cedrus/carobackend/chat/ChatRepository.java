package org.one_cedrus.carobackend.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> getChatMessagesBySenderOrderByTimeStampDesc(String sender, Pageable pageable);

    List<ChatMessage> getChatMessagesByReceiverOrderByTimeStampDesc(String receiver, Pageable pageable);
}
