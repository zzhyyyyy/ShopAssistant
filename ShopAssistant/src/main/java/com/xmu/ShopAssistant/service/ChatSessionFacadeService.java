package com.xmu.ShopAssistant.service;

import com.xmu.ShopAssistant.model.request.CreateChatSessionRequest;
import com.xmu.ShopAssistant.model.request.UpdateChatSessionRequest;
import com.xmu.ShopAssistant.model.response.CreateChatSessionResponse;
import com.xmu.ShopAssistant.model.response.GetChatSessionResponse;
import com.xmu.ShopAssistant.model.response.GetChatSessionsResponse;

public interface ChatSessionFacadeService {
    GetChatSessionsResponse getChatSessions();

    GetChatSessionResponse getChatSession(String chatSessionId);

    GetChatSessionsResponse getChatSessionsByAgentId(String agentId);

    CreateChatSessionResponse createChatSession(CreateChatSessionRequest request);

    void deleteChatSession(String chatSessionId);

    void updateChatSession(String chatSessionId, UpdateChatSessionRequest request);
}
