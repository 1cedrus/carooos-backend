package org.one_cedrus.carobackend.chat.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @JoinColumn(table = "_user", name = "username")
    private String sender;

    @ManyToOne
    @JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
    )
    @JsonIdentityReference(alwaysAsId = true)
    private Conversation conversation;

    private String content;

    private LocalDateTime timeStamp;

    public static Message create(
        String sender,
        Conversation conversation,
        String content
    ) {
        return Message.builder()
            .sender(sender)
            .conversation(conversation)
            .content(content)
            .timeStamp(LocalDateTime.now())
            .build();
    }
}
