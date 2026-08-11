package com.remindercat.agent.memory;

import java.time.LocalDateTime;

public record MemoryMessage(String role, String content, LocalDateTime createdTime) {
}
