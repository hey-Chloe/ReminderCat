package com.remindercat.agent.llm;

import java.util.Objects;

public class MockLLMClient implements LLMClient {

    private static final String DEFAULT_RESPONSE = """
            {
              "intent":"CREATE_TASK",
              "content":"开会",
              "remindTime":"2026-08-11T15:00:00"
            }
            """;

    private final String response;
    private volatile String lastPrompt;

    public MockLLMClient() {
        this(DEFAULT_RESPONSE);
    }

    public MockLLMClient(String response) {
        this.response = Objects.requireNonNull(response, "response不能为空");
    }

    @Override
    public String chat(String prompt) {
        lastPrompt = prompt;
        return response;
    }

    public String getLastPrompt() {
        return lastPrompt;
    }
}
