package com.xmu.ShopAssistant.controller;

import com.xmu.ShopAssistant.model.common.ApiResponse;
import com.xmu.ShopAssistant.model.request.RagEvaluationRequest;
import com.xmu.ShopAssistant.model.response.RagEvaluationResponse;
import com.xmu.ShopAssistant.service.RagEvaluationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
@AllArgsConstructor
public class RagEvaluationController {

    private final RagEvaluationService ragEvaluationService;

    @PostMapping("/evaluate")
    public ApiResponse<RagEvaluationResponse> evaluate(@RequestBody RagEvaluationRequest request) {
        return ApiResponse.success(ragEvaluationService.evaluate(request));
    }
}
