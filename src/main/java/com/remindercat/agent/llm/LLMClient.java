package com.remindercat.agent.llm;

@FunctionalInterface
public interface LLMClient {

    String chat(String prompt);
}
