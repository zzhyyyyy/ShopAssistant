package com.xmu.ShopAssistant.model.response;

import com.xmu.ShopAssistant.model.vo.ChatSessionVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetChatSessionsResponse {
    private ChatSessionVO[] chatSessions;
}
