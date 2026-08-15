package com.remindercat.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class WeChatMessageRepository {

    private final WeChatMessageMapper weChatMessageMapper;

    public WeChatMessageRepository(WeChatMessageMapper weChatMessageMapper) {
        this.weChatMessageMapper = weChatMessageMapper;
    }

    /**
     * 幂等登记：首次收到返回 true，重复 msgId 返回 false。
     * 基于数据库主键冲突保证并发下的原子性。
     */
    public boolean markReceived(String msgId, String userId, String msgType, LocalDateTime receivedTime) {
        return weChatMessageMapper.insertIfAbsent(msgId, userId, msgType, receivedTime) == 1;
    }
}
