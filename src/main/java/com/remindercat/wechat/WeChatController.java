package com.remindercat.wechat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentRuntime;
import com.remindercat.agent.AgentState;
import com.remindercat.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wechat")
public class WeChatController {

    private final AgentRuntime agentRuntime;

    public WeChatController(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    @PostMapping("/message")
    public Result<AgentResult<?>> receiveMessage(@Valid @RequestBody WeChatMessage message) {
        AgentState state = AgentState.builder()
                .userId(message.getUserId())
                .input(message.getMessage())
                .build();

        return Result.success(agentRuntime.execute(state));
    }
}
