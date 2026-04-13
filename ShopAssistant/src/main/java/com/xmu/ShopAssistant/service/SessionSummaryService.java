package com.xmu.ShopAssistant.service;

public interface SessionSummaryService {
    void refreshSessionSummary(String agentId, String sessionId);

    String getSessionSummary(String sessionId);
}
