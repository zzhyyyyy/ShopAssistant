package com.xmu.ShopAssistant.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AgentMemory {
    public enum Status {
        ACTIVE,
        SUPERSEDED,
        CONFLICTED
    }

    private String id;
    private String agentId;
    private String memoryKey;
    private String memoryValue;
    private String fact;
    private Double confidence;
    private String status;
    private String supersededByMemoryId;
    private String sourceSessionId;
    private Integer evidenceCount;
    private LocalDateTime lastConfirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
