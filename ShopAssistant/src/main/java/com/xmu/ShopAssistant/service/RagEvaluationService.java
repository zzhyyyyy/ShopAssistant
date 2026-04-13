package com.xmu.ShopAssistant.service;

import com.xmu.ShopAssistant.model.request.RagEvaluationRequest;
import com.xmu.ShopAssistant.model.response.RagEvaluationResponse;

public interface RagEvaluationService {
    RagEvaluationResponse evaluate(RagEvaluationRequest request);
}
