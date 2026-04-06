package com.xmu.ShopAssistant.service;

import com.xmu.ShopAssistant.model.request.CreateKnowledgeBaseRequest;
import com.xmu.ShopAssistant.model.request.UpdateKnowledgeBaseRequest;
import com.xmu.ShopAssistant.model.response.CreateKnowledgeBaseResponse;
import com.xmu.ShopAssistant.model.response.GetKnowledgeBasesResponse;

public interface KnowledgeBaseFacadeService {
    GetKnowledgeBasesResponse getKnowledgeBases();

    CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request);

    void deleteKnowledgeBase(String knowledgeBaseId);

    void updateKnowledgeBase(String knowledgeBaseId, UpdateKnowledgeBaseRequest request);
}

