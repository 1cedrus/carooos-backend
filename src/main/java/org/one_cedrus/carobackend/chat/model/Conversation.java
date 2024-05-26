package org.one_cedrus.carobackend.chat.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue
    private Long id;

    private Integer numOfMessages;

    @ManyToOne
    private Message lastMessage;

    @OneToMany(mappedBy = "conversation")
    private Set<Message> messages;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private Set<UserConversation> userConversations = new HashSet<>();
}
