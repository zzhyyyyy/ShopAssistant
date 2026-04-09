package com.xmu.ShopAssistant.mapper;

import com.xmu.ShopAssistant.model.entity.AgentMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentMemoryMapper {
    int insert(AgentMemory agentMemory);

    List<AgentMemory> selectByAgentId(@Param("agentId") String agentId, @Param("limit") int limit);

    AgentMemory selectLatestActiveByAgentIdAndMemoryKey(@Param("agentId") String agentId, @Param("memoryKey") String memoryKey);

    int updateById(AgentMemory agentMemory);
}
