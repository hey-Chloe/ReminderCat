package com.remindercat.service;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

    public String processMessage(String message) {
        return "收到，我会提醒你";
    }
}
