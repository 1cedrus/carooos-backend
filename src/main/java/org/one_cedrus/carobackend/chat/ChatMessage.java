package org.one_cedrus.carobackend.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;

    @JoinColumn(table = "_user", name = "username")
    private String sender;

    @JoinColumn(table = "_user", name = "username")
    private String receiver;

    private String content;

    private LocalDateTime timeStamp;
}
