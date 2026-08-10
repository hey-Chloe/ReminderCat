package com.remindercat;

import com.remindercat.agent.llm.LLMClient;
import com.remindercat.agent.llm.MockLLMClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LLMClientTests {

    @Test
    void mockClientShouldReturnFixedStructuredResponse() {
        LLMClient client = new MockLLMClient();

        String response = client.chat("任意提示词");

        assertEquals("""
                {
                  "intent":"CREATE_TASK",
                  "content":"开会",
                  "remindTime":"2026-08-11T15:00:00"
                }
                """, response);
    }

    @Test
    void llmClientImplementationShouldBeReplaceable() {
        LLMClient createTaskClient = new MockLLMClient("{\"intent\":\"CREATE_TASK\"}");
        LLMClient queryTaskClient = prompt -> "{\"intent\":\"QUERY_TASK\"}";

        assertEquals("{\"intent\":\"CREATE_TASK\"}", createTaskClient.chat("创建任务"));
        assertEquals("{\"intent\":\"QUERY_TASK\"}", queryTaskClient.chat("查询任务"));
    }
}
