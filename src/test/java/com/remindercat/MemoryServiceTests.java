package com.remindercat;

import com.remindercat.agent.memory.MemoryMessage;
import com.remindercat.agent.memory.MemoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryServiceTests {

    @Test
    void shouldSaveConversationByUser() {
        MemoryService memoryService = new MemoryService();

        memoryService.saveUserMessage("memory-user", "明天提醒我开会");
        memoryService.saveAssistantMessage("memory-user", "任务创建成功");

        List<MemoryMessage> messages = memoryService.getRecentMessages("memory-user");
        assertThat(messages).extracting(MemoryMessage::role)
                .containsExactly("user", "assistant");
        assertThat(messages).extracting(MemoryMessage::content)
                .containsExactly("明天提醒我开会", "任务创建成功");
        assertThat(memoryService.getRecentMessages("another-user")).isEmpty();
    }

    @Test
    void shouldKeepOnlyLatestTwentyMessages() {
        MemoryService memoryService = new MemoryService();

        for (int index = 1; index <= 25; index++) {
            memoryService.saveUserMessage("bounded-user", "message-" + index);
        }

        List<MemoryMessage> messages = memoryService.getRecentMessages("bounded-user");
        assertThat(messages).hasSize(20);
        assertThat(messages.getFirst().content()).isEqualTo("message-6");
        assertThat(messages.getLast().content()).isEqualTo("message-25");
    }
}
