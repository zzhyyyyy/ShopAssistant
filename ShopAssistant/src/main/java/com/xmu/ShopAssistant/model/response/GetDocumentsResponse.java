package com.xmu.ShopAssistant.model.response;

import com.xmu.ShopAssistant.model.vo.DocumentVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDocumentsResponse {
    private DocumentVO[] documents;
}

