package com.remindercat;

import com.remindercat.agent.Intent;
import com.remindercat.agent.llm.MockLLMClient;
import com.remindercat.agent.parser.LLMIntentParser;
import com.remindercat.agent.schema.TaskIntent;
import com.remindercat.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMIntentParserTests {

    @Test
    void shouldParseStructuredJsonIntoTaskIntent() {
        MockLLMClient llmClient = new MockLLMClient();
        LLMIntentParser parser = new LLMIntentParser(llmClient, new ObjectMapper());

        TaskIntent taskIntent = parser.parse("明天下午3点提醒我开会");

        assertEquals(Intent.CREATE_TASK, taskIntent.getIntent());
        assertEquals("开会", taskIntent.getContent());
        assertEquals(LocalDateTime.of(2026, 8, 11, 15, 0), taskIntent.getRemindTime());
    }

    @Test
    void shouldCallMockLlmClientWithConstructedPrompt() {
        MockLLMClient llmClient = new MockLLMClient();
        LLMIntentParser parser = new LLMIntentParser(llmClient, new ObjectMapper());

        parser.parse("查询我的提醒");

        assertTrue(llmClient.getLastPrompt().contains("用户输入：查询我的提醒"));
        assertTrue(llmClient.getLastPrompt().contains("CREATE_TASK、QUERY_TASK或UNKNOWN"));
    }

    @Test
    void shouldThrowBusinessExceptionForInvalidJson() {
        MockLLMClient llmClient = new MockLLMClient("invalid-json");
        LLMIntentParser parser = new LLMIntentParser(llmClient, new ObjectMapper());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parser.parse("创建提醒")
        );

        assertEquals(500, exception.getCode());
        assertEquals("LLM返回格式非法", exception.getMessage());
    }
}
