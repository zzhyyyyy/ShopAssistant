package com.xmu.ShopAssistant.model.response;

import com.xmu.ShopAssistant.model.vo.ChatMessageVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetChatMessagesResponse {
    private ChatMessageVO[] chatMessages;
}

