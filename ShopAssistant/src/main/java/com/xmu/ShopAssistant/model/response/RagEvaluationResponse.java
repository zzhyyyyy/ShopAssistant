package com.xmu.ShopAssistant.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagEvaluationResponse {
    private Integer topK;
    private Integer totalCases;
    private Metrics metrics;
    private List<CaseResult> caseResults;

    @Data
    @Builder
    public static class Metrics {
        private Double hitRateAtK;
        private Double avgRecallAtK;
        private Double mrrAtK;
        private Double ndcgAtK;
    }

    @Data
    @Builder
    public static class CaseResult {
        private String kbId;
        private String query;
        private List<String> retrievedDocIds;
        private List<String> relevantDocIds;
        private Double hitAtK;
        private Double recallAtK;
        private Double reciprocalRank;
        private Double ndcgAtK;
    }
}
