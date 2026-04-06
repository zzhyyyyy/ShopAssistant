package com.xmu.ShopAssistant.event.listener;

import com.xmu.ShopAssistant.agent.ShopAi;
import com.xmu.ShopAssistant.agent.ShopAiFactory;
import com.xmu.ShopAssistant.event.ChatEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {

    private final ShopAiFactory shopAiFactory;

    @Async
    @EventListener
    public void handle(ChatEvent event) {
        // 创建一个 Agent 实例处理聊天事件
        ShopAi shopAi = shopAiFactory.create(event.getAgentId(), event.getSessionId());
        shopAi.run();
    }
}
