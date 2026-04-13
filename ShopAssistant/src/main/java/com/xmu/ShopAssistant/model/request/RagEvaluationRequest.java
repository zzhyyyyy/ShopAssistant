package com.xmu.ShopAssistant.model.request;

import lombok.Data;

import java.util.List;

@Data
public class RagEvaluationRequest {
    private Integer topK;
    private List<EvalCase> cases;

    @Data
    public static class EvalCase {
        private String kbId;
        private String query;
        private List<String> relevantDocIds;
    }
}
