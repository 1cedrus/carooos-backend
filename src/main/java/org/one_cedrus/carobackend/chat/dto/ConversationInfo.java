package org.one_cedrus.carobackend.chat.dto;

import lombok.Builder;
import lombok.Data;
import org.one_cedrus.carobackend.chat.model.Message;

import java.util.List;

@Data
@Builder
public class ConversationInfo {
    private Long cid;
    private List<String> peers;
    private Boolean seen;
    private Message lastMessage;
}
