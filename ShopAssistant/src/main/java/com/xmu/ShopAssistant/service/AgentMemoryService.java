package com.xmu.ShopAssistant.service;

import java.util.List;

public interface AgentMemoryService {
    void rememberProfileFacts(String agentId, String sessionId, String userText);

    List<String> listFactsByAgentId(String agentId, int limit);
}
