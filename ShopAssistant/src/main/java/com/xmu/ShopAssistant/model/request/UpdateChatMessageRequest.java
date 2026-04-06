package com.xmu.ShopAssistant.model.request;

import com.xmu.ShopAssistant.model.dto.ChatMessageDTO;
import lombok.Data;

@Data
public class UpdateChatMessageRequest {
    private String content;
    private ChatMessageDTO.MetaData metadata;
}

