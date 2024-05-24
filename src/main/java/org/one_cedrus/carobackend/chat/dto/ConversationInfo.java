package org.one_cedrus.carobackend.chat.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.one_cedrus.carobackend.chat.model.Message;

@Data
@Builder
public class ConversationInfo {

    private Long cid;
    private List<String> peers;
    private Boolean seen;
    private Message lastMessage;
}
