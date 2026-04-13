package com.xmu.ShopAssistant.service;

import com.xmu.ShopAssistant.model.entity.ChunkBgeM3;

import java.util.List;

public interface RagService {
    float[] embed(String text);

    List<String> similaritySearch(String kbId, String title);

    List<ChunkBgeM3> similaritySearchChunks(String kbId, String query, int limit);
}
