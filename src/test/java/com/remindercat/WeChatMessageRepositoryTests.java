package com.remindercat;

import com.remindercat.repository.WeChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WeChatMessageRepositoryTests {

    @Autowired
    private WeChatMessageRepository weChatMessageRepository;

    @Test
    void shouldAcceptFirstMessageAndRejectDuplicateMsgId() {
        String msgId = "msg-" + UUID.randomUUID();

        assertThat(weChatMessageRepository.markReceived(msgId, "user-1", "text", LocalDateTime.now())).isTrue();
        assertThat(weChatMessageRepository.markReceived(msgId, "user-1", "text", LocalDateTime.now())).isFalse();
    }
}
