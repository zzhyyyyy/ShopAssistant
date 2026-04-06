package com.xmu.ShopAssistant.mapper;

import com.xmu.ShopAssistant.model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface KnowledgeBaseMapper {
    int insert(KnowledgeBase knowledgeBase);

    KnowledgeBase selectById(String id);

    List<KnowledgeBase> selectAll();

    List<KnowledgeBase> selectByIdBatch(List<String> ids);

    int deleteById(String id);

    int updateById(KnowledgeBase knowledgeBase);
}
