package com.xmu.ShopAssistant.service.impl;

import com.xmu.ShopAssistant.mapper.AgentMemoryMapper;
import com.xmu.ShopAssistant.model.entity.AgentMemory;
import com.xmu.ShopAssistant.service.AgentMemoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class AgentMemoryServiceImpl implements AgentMemoryService {

    private static final int DEFAULT_FACT_LIMIT = 20;
    private static final int MAX_VALUE_LEN = 64;
    private static final double MIN_CONFIDENCE = 0.50;
    private static final double MAX_CONFIDENCE = 0.98;
    private static final int RECENT_DAYS_THRESHOLD = 7;

    // 动态分数参数
    private static final double IDENTITY_BASE = 0.70;
    private static final double EDUCATION_BASE = 0.70;
    private static final double ATTRIBUTE_BASE = 0.65;
    private static final double PREFERENCE_BASE = 0.75;

    private static final double BONUS_PER_EVIDENCE = 0.03;
    private static final double BONUS_EVIDENCE_CAP = 0.18;
    private static final double BONUS_CROSS_SESSION = 0.10;
    private static final double BONUS_STRONG_ASSERTION = 0.05;
    private static final double BONUS_RECENT_RECONFIRM = 0.05;

    private static final double PENALTY_UNCERTAIN = 0.10;
    private static final double PENALTY_CONFLICT = 0.20;

    private static final double DECAY_PER_30_DAYS = 0.03;
    private static final double DECAY_CAP = 0.20;

    // 通用自述模式
    private static final Pattern IDENTITY_PATTERN =
            Pattern.compile("^我(?:是|是一名|是一位)(.{1,60}?)(?:的?学生)?(?:如果|并且|而且|想|希望|请问|能|可以|$)");
    private static final Pattern EDUCATION_PATTERN = Pattern.compile("^我在读(.{1,60})$");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("^我的(.{1,20})是(.{1,60})$");

    // 通用偏好模式
    private static final Pattern LIKE_PATTERN = Pattern.compile("^我(?:最)?(?:喜欢|偏好|爱)(.{1,40})$");
    private static final Pattern DISLIKE_PATTERN = Pattern.compile("^我(?:最)?(?:讨厌|不喜欢|厌恶|不爱)(.{1,40})$");

    private final AgentMemoryMapper agentMemoryMapper;

    @Override
    public void rememberProfileFacts(String agentId, String sessionId, String userText) {
        if (!StringUtils.hasText(agentId) || !StringUtils.hasText(sessionId) || !StringUtils.hasText(userText)) {
            return;
        }
        List<ExtractedMemory> extractedMemories = extractMemories(userText);
        if (extractedMemories.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        for (ExtractedMemory extracted : extractedMemories) {
            AgentMemory existing = agentMemoryMapper.selectLatestActiveByAgentIdAndMemoryKey(agentId, extracted.memoryKey());

            // 相同结论：提升证据和置信度
            if (existing != null && extracted.memoryValue().equals(existing.getMemoryValue())) {
                int evidenceCount = (existing.getEvidenceCount() == null ? 1 : existing.getEvidenceCount()) + 1;
                double confidence = computeConfidence(extracted, existing, sessionId, evidenceCount, false, now);
                AgentMemory update = AgentMemory.builder()
                        .id(existing.getId())
                        .fact(extracted.fact())
                        .confidence(confidence)
                        .sourceSessionId(sessionId)
                        .evidenceCount(evidenceCount)
                        .lastConfirmedAt(now)
                        .build();
                agentMemoryMapper.updateById(update);
                continue;
            }

            // 冲突：先将旧 ACTIVE 降级，再写入新 ACTIVE
            if (existing != null) {
                AgentMemory demote = AgentMemory.builder()
                        .id(existing.getId())
                        .status(AgentMemory.Status.SUPERSEDED.name())
                        .build();
                agentMemoryMapper.updateById(demote);
            }

            int initialEvidenceCount = 1;
            double confidence = computeConfidence(extracted, existing, sessionId, initialEvidenceCount, existing != null, now);
            AgentMemory insert = AgentMemory.builder()
                    .agentId(agentId)
                    .memoryKey(extracted.memoryKey())
                    .memoryValue(extracted.memoryValue())
                    .fact(extracted.fact())
                    .confidence(confidence)
                    .status(AgentMemory.Status.ACTIVE.name())
                    .sourceSessionId(sessionId)
                    .evidenceCount(initialEvidenceCount)
                    .lastConfirmedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            agentMemoryMapper.insert(insert);

            // 记录冲突链路
            if (existing != null) {
                AgentMemory link = AgentMemory.builder()
                        .id(existing.getId())
                        .supersededByMemoryId(insert.getId())
                        .build();
                agentMemoryMapper.updateById(link);
            }
        }
    }

    @Override
    public List<String> listFactsByAgentId(String agentId, int limit) {
        if (!StringUtils.hasText(agentId)) {
            return List.of();
        }
        int safeLimit = limit <= 0 ? DEFAULT_FACT_LIMIT : limit;
        List<AgentMemory> memories = agentMemoryMapper.selectByAgentId(agentId, safeLimit);

        // 仅注入结构化记忆；过滤历史遗留的 profile.statement.* 泛化记忆，避免干扰新记忆
        Map<String, AgentMemory> latestByKey = new LinkedHashMap<>();
        for (AgentMemory memory : memories) {
            if (memory == null || !StringUtils.hasText(memory.getMemoryKey())) {
                continue;
            }
            String key = memory.getMemoryKey();
            if (key.startsWith("profile.statement.")) {
                continue;
            }
            if (memory.getConfidence() != null && memory.getConfidence() < 0.8) {
                continue;
            }
            latestByKey.putIfAbsent(key, memory);
        }

        return latestByKey.values().stream()
                .map(AgentMemory::getFact)
                .filter(StringUtils::hasText)
                .toList();
    }

    private double computeConfidence(
            ExtractedMemory extracted,
            AgentMemory existing,
            String sessionId,
            int evidenceCount,
            boolean conflict,
            LocalDateTime now
    ) {
        double score = extracted.baseConfidence();

        if (extracted.strongAssertion()) {
            score += BONUS_STRONG_ASSERTION;
        }
        if (extracted.uncertain()) {
            score -= PENALTY_UNCERTAIN;
        }

        if (evidenceCount > 1) {
            score += Math.min(BONUS_EVIDENCE_CAP, (evidenceCount - 1) * BONUS_PER_EVIDENCE);
        }

        if (existing != null) {
            if (!sameSession(existing.getSourceSessionId(), sessionId)) {
                score += BONUS_CROSS_SESSION;
            }
            if (existing.getLastConfirmedAt() != null) {
                long days = ChronoUnit.DAYS.between(existing.getLastConfirmedAt(), now);
                if (days <= RECENT_DAYS_THRESHOLD) {
                    score += BONUS_RECENT_RECONFIRM;
                }
                if (days > 0) {
                    score -= Math.min(DECAY_CAP, (days / 30.0) * DECAY_PER_30_DAYS);
                }
            }
        }

        if (conflict) {
            score -= PENALTY_CONFLICT;
        }

        return clamp(score, MIN_CONFIDENCE, MAX_CONFIDENCE);
    }

    private List<ExtractedMemory> extractMemories(String text) {
        String[] pieces = text.split("[。！？；;，,\\n]");
        Map<String, ExtractedMemory> memoryByKey = new LinkedHashMap<>();

        for (String piece : pieces) {
            String sentence = normalizeSentence(piece);
            if (!StringUtils.hasText(sentence) || sentence.length() > 120) {
                continue;
            }

            ExtractedMemory memory = parseStructuredMemory(sentence);
            if (memory == null) {
                continue;
            }
            // 同一轮里同 key 多次出现时，以最后一次为准
            memoryByKey.put(memory.memoryKey(), memory);
        }
        return new ArrayList<>(memoryByKey.values());
    }

    private ExtractedMemory parseStructuredMemory(String sentence) {
        boolean uncertain = hasUncertainHint(sentence);
        boolean strongAssertion = hasStrongAssertionHint(sentence);

        Matcher identityMatcher = IDENTITY_PATTERN.matcher(sentence);
        if (identityMatcher.find()) {
            String value = normalizeValue(identityMatcher.group(1));
            if (StringUtils.hasText(value)) {
                return new ExtractedMemory("profile.identity", value, sentence, IDENTITY_BASE, uncertain, strongAssertion);
            }
        }

        Matcher educationMatcher = EDUCATION_PATTERN.matcher(sentence);
        if (educationMatcher.find()) {
            String value = normalizeValue(educationMatcher.group(1));
            if (StringUtils.hasText(value)) {
                return new ExtractedMemory("profile.education.current", value, sentence, EDUCATION_BASE, uncertain, strongAssertion);
            }
        }

        Matcher attributeMatcher = ATTRIBUTE_PATTERN.matcher(sentence);
        if (attributeMatcher.find()) {
            String attr = normalizeKeyFragment(attributeMatcher.group(1));
            String value = normalizeValue(attributeMatcher.group(2));
            if (StringUtils.hasText(attr) && StringUtils.hasText(value)) {
                return new ExtractedMemory("profile.attr." + attr, value, sentence, ATTRIBUTE_BASE, uncertain, strongAssertion);
            }
        }

        Matcher likeMatcher = LIKE_PATTERN.matcher(sentence);
        if (likeMatcher.find()) {
            String target = normalizePreferenceTarget(likeMatcher.group(1));
            if (StringUtils.hasText(target)) {
                return new ExtractedMemory("preference." + target, "like", sentence, PREFERENCE_BASE, uncertain, strongAssertion);
            }
        }

        Matcher dislikeMatcher = DISLIKE_PATTERN.matcher(sentence);
        if (dislikeMatcher.find()) {
            String target = normalizePreferenceTarget(dislikeMatcher.group(1));
            if (StringUtils.hasText(target)) {
                return new ExtractedMemory("preference." + target, "dislike", sentence, PREFERENCE_BASE, uncertain, strongAssertion);
            }
        }

        return null;
    }

    private boolean hasUncertainHint(String sentence) {
        if (!StringUtils.hasText(sentence)) {
            return false;
        }
        return sentence.contains("可能")
                || sentence.contains("大概")
                || sentence.contains("好像")
                || sentence.contains("也许")
                || sentence.contains("应该是");
    }

    private boolean hasStrongAssertionHint(String sentence) {
        if (!StringUtils.hasText(sentence)) {
            return false;
        }
        return sentence.contains("就是")
                || sentence.contains("一直")
                || sentence.contains("确定")
                || sentence.contains("肯定")
                || sentence.contains("必须");
    }

    private boolean sameSession(String oldSessionId, String newSessionId) {
        return StringUtils.hasText(oldSessionId)
                && StringUtils.hasText(newSessionId)
                && oldSessionId.equals(newSessionId);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalizePreferenceTarget(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String cleaned = normalizeKeyFragment(raw);
        // 将“吃苹果/喝咖啡”等归一为“苹果/咖啡”，以便跨会话冲突判定
        cleaned = cleaned.replaceFirst("^(吃|喝|看|玩|用|做|学|去)", "");
        return cleaned;
    }

    private String normalizeKeyFragment(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.trim()
                .toLowerCase()
                .replaceAll("[，,。.！!？?、：:；;\\s]+", "")
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5_-]", "");
    }

    private String normalizeValue(String text) {
        String value = normalizeSentence(text);
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.length() > MAX_VALUE_LEN) {
            return value.substring(0, MAX_VALUE_LEN);
        }
        return value;
    }

    private String normalizeSentence(String sentence) {
        if (!StringUtils.hasText(sentence)) {
            return "";
        }
        return sentence.trim().replaceAll("\\s+", " ");
    }

    private record ExtractedMemory(
            String memoryKey,
            String memoryValue,
            String fact,
            double baseConfidence,
            boolean uncertain,
            boolean strongAssertion
    ) {
    }
}
