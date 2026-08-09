package com.remindercat.agent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentResult<T> {

    private final boolean success;

    private final Intent intent;

    private final String message;

    private final T data;

    public static <T> AgentResult<T> success(Intent intent, String message, T data) {
        return new AgentResult<>(true, intent, message, data);
    }

    public static <T> AgentResult<T> failure(Intent intent, String message) {
        return new AgentResult<>(false, intent, message, null);
    }
}
