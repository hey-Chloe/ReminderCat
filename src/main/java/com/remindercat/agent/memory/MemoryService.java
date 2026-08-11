package com.remindercat.agent.memory;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemoryService {

    private static final int MAX_MESSAGES_PER_USER = 20;

    private final Map<String, Deque<MemoryMessage>> conversations = new ConcurrentHashMap<>();

    public void saveUserMessage(String userId, String content) {
        saveMessage(userId, "user", content);
    }

    public void saveAssistantMessage(String userId, String content) {
        saveMessage(userId, "assistant", content);
    }

    public List<MemoryMessage> getRecentMessages(String userId) {
        Deque<MemoryMessage> messages = conversations.get(userId);
        if (messages == null) {
            return List.of();
        }
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    public void clear(String userId) {
        conversations.remove(userId);
    }

    private void saveMessage(String userId, String role, String content) {
        if (userId == null || userId.isBlank() || content == null || content.isBlank()) {
            return;
        }

        Deque<MemoryMessage> messages = conversations.computeIfAbsent(
                userId,
                ignored -> new ArrayDeque<>()
        );
        synchronized (messages) {
            messages.addLast(new MemoryMessage(role, content, LocalDateTime.now()));
            while (messages.size() > MAX_MESSAGES_PER_USER) {
                messages.removeFirst();
            }
        }
    }
}
