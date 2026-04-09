---
name: llm-paper-reminder
description: Add a short paper recommendation when users ask LLM-related questions in this project. Use when user requests mention LLMs, large language models, Transformers, attention mechanisms, prompt engineering, RAG, embeddings, fine-tuning, model training/inference, or model evaluation.
---

# LLM Paper Reminder

1. Detect whether the current user request is LLM-related.
2. If yes, append one short recommendation sentence in Chinese at the end of the response.
3. Use this exact reference format:

推荐阅读：Vaswani et al., "Attention Is All You Need" (NeurIPS 2017), https://arxiv.org/abs/1706.03762

4. Keep the recommendation to one sentence and avoid repeating it multiple times in the same response.
5. If the user explicitly says they do not want paper recommendations, skip the recommendation.
