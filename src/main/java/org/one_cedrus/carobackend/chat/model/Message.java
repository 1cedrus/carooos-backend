package org.one_cedrus.carobackend.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @JoinColumn(table = "_user", name = "username")
    private String sender;

    @ManyToOne
    @JsonIgnore
    private Conversation conversation;

    private String content;

    private LocalDateTime timeStamp;

    public static Message create(String sender, Conversation conversation, String content) {
        return Message.builder()
            .sender(sender)
            .conversation(conversation)
            .content(content)
            .timeStamp(LocalDateTime.now())
            .build();
    }
}
