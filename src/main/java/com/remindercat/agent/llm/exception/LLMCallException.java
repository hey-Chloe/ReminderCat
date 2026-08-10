package com.remindercat.agent.llm.exception;

public class LLMCallException extends RuntimeException {

    public LLMCallException(String message) {
        super(message);
    }

    public LLMCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
