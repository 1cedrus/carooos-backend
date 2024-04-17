package org.one_cedrus.carobackend.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawMessage {
    private String sender;
    private Long cid;
    private String content;
}
