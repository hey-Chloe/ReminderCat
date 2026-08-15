package com.remindercat.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface WeChatMessageMapper {

    @Insert("""
            INSERT INTO wechat_messages (msg_id, user_id, msg_type, received_time)
            VALUES (#{msgId}, #{userId}, #{msgType}, #{receivedTime})
            ON CONFLICT (msg_id) DO NOTHING
            """)
    int insertIfAbsent(
            @Param("msgId") String msgId,
            @Param("userId") String userId,
            @Param("msgType") String msgType,
            @Param("receivedTime") LocalDateTime receivedTime
    );
}
