package com.xmu.ShopAssistant.service;

import com.xmu.ShopAssistant.model.request.CreateAgentRequest;
import com.xmu.ShopAssistant.model.request.UpdateAgentRequest;
import com.xmu.ShopAssistant.model.response.CreateAgentResponse;
import com.xmu.ShopAssistant.model.response.GetAgentsResponse;

public interface AgentFacadeService {
    GetAgentsResponse getAgents();

    CreateAgentResponse createAgent(CreateAgentRequest request);

    void deleteAgent(String agentId);

    void updateAgent(String agentId, UpdateAgentRequest request);
}
