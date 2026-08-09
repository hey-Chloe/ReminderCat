package com.remindercat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService {

    public String processMessage(String message) {
        int messageLength = message == null ? 0 : message.length();
        log.info("Processing reminder message, messageLength={}", messageLength);

        return "收到，我会提醒你";
    }
}
