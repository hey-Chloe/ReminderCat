package com.remindercat.controller;

import com.remindercat.common.response.Result;
import com.remindercat.dto.MessageRequest;
import com.remindercat.dto.MessageResponse;
import com.remindercat.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final AgentService agentService;

    @PostMapping("/message")
    public Result<MessageResponse> message(@Valid @RequestBody MessageRequest request) {
        String reply = agentService.processMessage(request.getMessage());

        MessageResponse response = new MessageResponse(
                request.getUserId(),
                reply
        );

        return Result.success(response);
    }
}
