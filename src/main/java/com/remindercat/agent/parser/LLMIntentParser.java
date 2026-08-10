package com.remindercat.agent.parser;

import com.remindercat.agent.Intent;
import com.remindercat.agent.llm.LLMClient;
import com.remindercat.agent.schema.TaskIntent;
import com.remindercat.common.exception.BusinessException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public class LLMIntentParser implements IntentParser {

    private final LLMClient llmClient;
    private final ObjectMapper objectMapper;

    public LLMIntentParser(LLMClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public TaskIntent parse(String input) {
        String response = llmClient.chat(buildPrompt(input));

        try {
            JsonNode root = objectMapper.readTree(response);
            Intent intent = Intent.valueOf(requiredText(root, "intent"));
            String content = optionalText(root, "content");
            String remindTimeText = optionalText(root, "remindTime");
            LocalDateTime remindTime = remindTimeText == null
                    ? null
                    : LocalDateTime.parse(remindTimeText);

            return TaskIntent.builder()
                    .intent(intent)
                    .content(content)
                    .remindTime(remindTime)
                    .build();
        } catch (Exception exception) {
            throw new BusinessException(500, "LLM返回格式非法");
        }
    }

    private String buildPrompt(String input) {
        return """
                请将用户输入解析为JSON，只返回以下字段：
                intent: CREATE_TASK、QUERY_TASK或UNKNOWN
                content: 提醒内容
                remindTime: ISO-8601格式时间
                用户输入：%s
                """.formatted(input);
    }

    private String requiredText(JsonNode root, String fieldName) {
        String value = optionalText(root, fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少字段: " + fieldName);
        }
        return value;
    }

    private String optionalText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asString();
    }
}
